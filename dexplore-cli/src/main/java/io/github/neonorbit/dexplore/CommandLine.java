/*
 * Copyright (C) 2022 NeonOrbit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.neonorbit.dexplore;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import io.github.neonorbit.dexplore.filter.ClassFilter;
import io.github.neonorbit.dexplore.filter.DexFilter;
import io.github.neonorbit.dexplore.filter.MethodFilter;
import io.github.neonorbit.dexplore.filter.ReferenceFilter;
import io.github.neonorbit.dexplore.filter.ReferenceTypes;
import io.github.neonorbit.dexplore.result.ClassData;
import io.github.neonorbit.dexplore.result.MethodData;
import io.github.neonorbit.dexplore.util.DexLog;
import io.github.neonorbit.dexplore.util.DexLogger;
import io.github.neonorbit.dexplore.util.DexUtils;
import jadx.api.JadxArgs;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;
import jadx.api.plugins.input.data.IClassData;
import jadx.plugins.input.dex.DexFileLoader;
import jadx.plugins.input.dex.DexInputOptions;
import jadx.plugins.input.dex.DexLoadResult;

import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
final class CommandLine extends JCommander {

  @Parameter(description = "files") @SuppressWarnings("all")
  private List<String> files = new ArrayList<>();

  @Parameter(names = {"-h", "--help"}, order = 0, help = true, description = "Print this help message")
  private boolean help = false;

  @Parameter(names = {"-m", "--maximum"}, order = 1, description = "Limit maximum results. Default: -1 (no limit)")
  private int maximum = -1;

  @Parameter(names = {"-t", "--type"}, order = 2, description = "Item to find: c: find class (default), m: find method")
  private String itemType = "c";

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  @Parameter(names = {"-c", "--classes"}, variableArity = true, order = 3, description = "List of classes to search in.")
  private List<String> classes = new ArrayList<>();

  @Parameter(names = {"-rt", "--ref-type"}, order = 4, description = "Reference types: a: all, s: string, t: type, f: field, m: method")
  private String refType = "";

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  @Parameter(names = {"-ref", "--references"}, variableArity = true, order = 5, description = "References: string, type, field or method names")
  private List<String> references = new ArrayList<>();

  @Parameter(names = {"-d", "--details"}, order = 6, description = "Print details: c: class details, m: method details only")
  private String printDetails = "";

  @Parameter(names = {"-s", "--sources"}, order = 7, description = "Generate java and smali source files")
  private boolean generate = false;

  @Parameter(names = {"-o", "--output"}, order = 8, description = "Output directory. Default: dexplore-out")
  private String output = "dexplore-out";

  @Parameter(names = {"-v", "--verbose"}, order = 9, description = "Verbose output")
  private boolean verbose = false;

  private static final Set<String> VALID_ITEM_TYPES = Set.of("c", "m");
  private static final Set<String> VALID_REFERENCE_TYPES = Set.of("a", "s", "t", "f", "m");

  private static final String TITLE = CommandLine.class.getPackage().getImplementationTitle();
  private static final String VERSION = CommandLine.class.getPackage().getImplementationVersion();

  private CommandLine(String[] args) {
    this.help |= args.length == 0;
    this.setProgramName(TITLE);
    this.addObject(this);
    this.parse(args);
  }

  public static void main(String[] args) {
    try {
      new CommandLine(args).apply();
    } catch (Throwable t) {
      String name = t.getClass().getSimpleName();
      System.err.println("\nError[" + name + "]: " + t.getMessage() + "\n");
    }
  }

  private void apply() {
    if (help || !validateArgs()) {
      usage(help);
      return;
    }
    if (verbose) {
      DexLog.enable();
      DexLog.setLogger(new DexLogger() {
        @Override
        public void debug(String msg) {
          System.out.println("D: " + msg);
        }
        @Override
        public void warn(String msg) {
          System.out.println("W: " + msg);
        }
      });
    }
    search();
  }

  private void search() {
    boolean isClass = itemType.equals("c");

    ReferenceTypes.Builder tBuilder = ReferenceTypes.builder();
    if (refType.contains("a")) tBuilder.addAll();
    if (refType.contains("s")) tBuilder.addString();
    if (refType.contains("t")) tBuilder.addTypeDes();
    if (refType.contains("f")) tBuilder.addFieldWithDetails();
    if (refType.contains("m")) tBuilder.addMethodWithDetails();
    ReferenceTypes types = tBuilder.build();
    ReferenceFilter filter = ReferenceFilter.containsAll(references.toArray(String[]::new));

    DexFilter dexFilter = DexFilter.MATCH_ALL;
    ClassFilter classFilter = ClassFilter.builder().setClasses(classes.toArray(String[]::new)).setReferenceTypes(types).setReferenceFilter(filter).build();
    MethodFilter methodFilter = isClass ? null : MethodFilter.builder().setReferenceTypes(types).setReferenceFilter(filter).build();

    for (String file : files) {
      System.out.println("File: " + file);
      if (isClass) {
        searchClasses(file, dexFilter, classFilter);
      } else {
        searchMethods(file, dexFilter, classFilter, methodFilter);
      }
      System.out.println();
    }
  }

  private void searchClasses(String file, DexFilter dexFilter, ClassFilter classFilter) {
    System.out.println("Searching...");
    List<ClassData> result = DexFactory.load(file).findClasses(dexFilter, classFilter, maximum);
    System.out.println("Result:");
    for (ClassData data : result) {
      System.out.println("+ Class: " + data);
      if (printDetails.equals("c")) {
        System.out.println("- ReferencePool: " + data.clazz);
        System.out.println(flattenReferencePool(data.getReferencePool()));
      }
    }
    if (result.isEmpty()) System.out.println("  [Not Found]");
    if (generate && !result.isEmpty()) {
      generateSources(file, result.stream().map(d -> DexUtils.javaToDexTypeName(d.clazz)).collect(Collectors.toSet()));
    }
  }

  private void searchMethods(String file, DexFilter dexFilter, ClassFilter classFilter, MethodFilter methodFilter) {
    System.out.println("Searching...");
    List<MethodData> result = DexFactory.load(file).findMethods(dexFilter, classFilter, methodFilter, maximum);
    System.out.println("Result:");
    for (MethodData data : result) {
      System.out.println("+ Method: " + data);
      if (printDetails.equals("m")) {
        System.out.println("- ReferencePool: " + data);
        System.out.println(flattenReferencePool(data.getReferencePool()));
      } else if (printDetails.equals("c")) {
        System.out.println("- ReferencePool: " + data.clazz);
        System.out.println(flattenReferencePool(data.getClassData().getReferencePool()));
      }
    }
    if (result.isEmpty()) System.out.println("  [Not Found]");
    if (generate && !result.isEmpty()) {
      generateSources(file, result.stream().map(d -> DexUtils.javaToDexTypeName(d.clazz)).collect(Collectors.toSet()));
    }
  }

  private void generateSources(String path, Set<String> classes) {
    final File file = new File(path);
    final File dir = createOutputDir(file.getName());
    if (dir == null) {
      System.out.println("!!--> Skipping...");
      return;
    }
    PrintStream err = System.err;
    System.out.println("Generating sources...");
    try {
      System.setErr(new PrintStream(new ByteArrayOutputStream(), true));
      JadxArgs args = new JadxArgs();
      args.setShowInconsistentCode(true);
      JadxDecompiler decompiler = new JadxDecompiler(args);
      decompiler.addCustomLoad(new DexLoadResult(new DexFileLoader(new DexInputOptions()).collectDexFiles(
              Collections.singletonList(file.toPath())
      ), null) {
        @Override
        public void visitClasses(Consumer<IClassData> consumer) {
          super.visitClasses(cls -> {
            if (classes.contains(cls.getType())) consumer.accept(cls);
          });
        }
      });
      decompiler.load();
      for (JavaClass node : decompiler.getClasses()) {
        System.out.print("-> [" + node.getName() + "]: ");
        String name = getValidName(dir, node.getName());
        try (Writer java = new FileWriter(new File(dir, name + ".java"));
             Writer smali = new FileWriter(new File(dir, name + ".smali"))) {
          java.write(node.getCode());
          smali.write(node.getSmali());
          System.out.print("done");
        } catch (Throwable t) {
          System.out.print("failed: " + t.getMessage());
        } finally {
          System.out.println();
        }
      }
    } catch (Throwable t) {
      System.out.println("Failed[" + t.getClass().getSimpleName() + "]: " + t.getMessage());
    } finally {
      System.setErr(err);
    }
  }

  private String getValidName(final File dir, final String name) {
    String valid = name;
    for (int i = 1; new File(dir, valid + ".java").exists() ||
                    new File(dir, valid + ".smali").exists(); i++) {
      valid = name + '_' + i;
    }
    return valid;
  }

  private File createOutputDir(String name) {
    final File dir = new File(output, name + "_sources");
    if (dir.exists()) {
      System.out.println("! Output directory exists: " + dir.getPath());
      Console console = System.console();
      if (console != null) {
        String line = console.readLine("> Overwrite[o] or Merge[m]?: ");
        if (line.equalsIgnoreCase("o") && !deleteDir(dir)) {
          System.out.println("! Failed to overwrite: " + dir.getPath());
        } else if (line.equalsIgnoreCase("m")) {
          return dir;
        }
      }
    }
    return (!dir.exists() && dir.mkdirs()) ? dir : null;
  }

  private static boolean deleteDir(File dir) {
    File[] files = dir.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory() && !Files.isSymbolicLink(file.toPath())) {
          deleteDir(file);
        } else if (!file.delete()) {
          return false;
        }
      }
    }
    return dir.delete();
  }

  private String flattenReferencePool(ReferencePool pool) {
    StringJoiner joiner = new StringJoiner("\n   ");
    joiner.add("   String References: ");
    if (pool.getStringSection().isEmpty()) joiner.add("  [EMPTY]");
    else pool.getStringSection().forEach(s -> joiner.add("  " + s.toString()));
    joiner.add("Type References: ");
    if (pool.getTypeSection().isEmpty()) joiner.add("  [EMPTY]");
    else pool.getTypeSection().forEach(t -> joiner.add("  " + t.toString()));
    joiner.add("Field References: ");
    if (pool.getFieldSection().isEmpty()) joiner.add("  [EMPTY]");
    else pool.getFieldSection().forEach(f -> joiner.add("  " + f.toString()));
    joiner.add("Method References: ");
    if (pool.getMethodSection().isEmpty()) joiner.add("  [EMPTY]");
    else pool.getMethodSection().forEach(m -> joiner.add("  " + m.toString()));
    return joiner.toString();
  }

  private boolean validateArgs() {
    if (output.isEmpty()) output = "dexplore-out";
    if (files == null || files.isEmpty()) {
      System.err.println("\n  Please provide input files\n");
      return false;
    }
    if (!VALID_ITEM_TYPES.contains(itemType)) {
      System.err.println("\n  Please enter correct item type\n");
      return false;
    }
    if (classes.isEmpty() && refType.isEmpty()) {
      System.err.println("\n  Please provide a search query\n");
      return false;
    }
    if (!refType.isEmpty()) {
      if (VALID_REFERENCE_TYPES.stream().noneMatch(refType::contains)) {
        System.err.println("\n  Please enter correct reference types\n");
        return false;
      }
      if (references.isEmpty()) {
        System.err.println("\n  Please provide references\n");
        return false;
      }
    } else if (!references.isEmpty()) {
      System.err.println("\n  Please provide reference types\n");
      return false;
    }
    return true;
  }

  private void usage(boolean help) {
    if (help) {
      getConsole().println(TITLE + " v" + VERSION + "\n");
    }
    usage();
  }

  @Override
  public void usage() {
    StringBuilder builder = new StringBuilder();
    StringJoiner output = new StringJoiner("\n");
    getUsageFormatter().usage(builder);
    output.add("Usage: " + TITLE + " <files> [options]");
    Arrays.stream(builder.toString().split("\n"))
          .filter(s -> !s.isEmpty() &&
                       !s.trim().startsWith("Usage: ") &&
                       !s.trim().startsWith("Default: "))
          .forEach(output::add);
    getConsole().println(output.toString());
  }
}
