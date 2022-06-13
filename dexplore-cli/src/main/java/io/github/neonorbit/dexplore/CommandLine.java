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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
final class CommandLine extends JCommander {

  @Parameter(description = "files") @SuppressWarnings("all")
  private List<String> files = new ArrayList<>();

  @Parameter(names = {"-h", "--help"}, order = 0, help = true, description = "Print this help message")
  private boolean help = false;

  @Parameter(names = {"-m", "--maximum"}, order = 1, description = "Maximum results")
  private int maximum = 1;

  @Parameter(names = {"-d", "--print-details"}, order = 1, description = "Print details: c: class details, m: method details only, n: none (default)")
  private String printDetails = "n";

  @Parameter(names = {"-t", "--type"}, order = 2, description = "Item to find: c: to find class, m: to find method")
  private String itemType = "";

  @Parameter(names = {"-rt", "--ref-type"}, order = 3, description = "Reference types: a: all (default), s: string, t: type, f: field, m: method")
  private String refType = "a";

  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  @Parameter(names = {"-ref", "--reference"}, variableArity = true, order = 4, description = "References: string, type, field or method names")
  private List<String> references = new ArrayList<>();

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
      System.err.println("\nError: " + t.getMessage() + "\n");
    }
  }

  private void apply() {
    if (help || !validateArgs()) {
      usage(help);
      return;
    }

    boolean isMethod = itemType.equals("m");

    ReferenceTypes.Builder tBuilder = ReferenceTypes.builder();
    if (refType.contains("a")) tBuilder.addAll();
    if (refType.contains("s")) tBuilder.addString();
    if (refType.contains("t")) tBuilder.addTypeDes();
    if (refType.contains("f")) tBuilder.addFieldWithDetails();
    if (refType.contains("m")) tBuilder.addMethodWithDetails();
    ReferenceTypes types = tBuilder.build();

    ReferenceFilter filter = ReferenceFilter.containsAll(references.toArray(String[]::new));

    DexFilter dexFilter = DexFilter.MATCH_ALL;

    ClassFilter classFilter = ClassFilter.builder()
                                         .setReferenceTypes(types)
                                         .setReferenceFilter(filter)
                                         .build();


    MethodFilter methodFilter = !isMethod ? null :
                                 MethodFilter.builder()
                                             .setReferenceTypes(types)
                                             .setReferenceFilter(filter)
                                             .build();

    List<String> result;
    if (!isMethod) {
      result = files.stream()
                    .flatMap(file -> DexFactory.load(file)
                            .findClasses(dexFilter, classFilter, maximum).stream())
                    .map(d -> {
                      String str = ' ' + d.toString();
                      if (printDetails.equals("c")) {
                        str += '\n' + flattenReferencePool(d.getReferencePool()) + '\n';
                      }
                      return str;
                    })
                    .collect(Collectors.toList());
    } else {
      result = files.stream()
                    .flatMap(file -> DexFactory.load(file)
                            .findMethods(dexFilter, classFilter, methodFilter, maximum).stream())
                    .map(d -> {
                      String str = ' ' + d.toString();
                      if (!printDetails.equals("n")) {
                        str += '\n';
                        str += printDetails.equals("m") ?
                                 flattenReferencePool(d.getReferencePool()) :
                                 flattenReferencePool(d.getClassData().getReferencePool());
                        str += '\n';
                      }
                      return str;
                    })
                    .collect(Collectors.toList());
    }

    if (!result.isEmpty()) {
      System.out.println("\nResult:");
      result.forEach(System.out::println);
    } else {
      System.out.println("\n  Not Found!");
    }
  }

  private String flattenReferencePool(ReferencePool pool) {
    StringJoiner joiner = new StringJoiner("\n   ");
    joiner.add("   String References: ");
    pool.getStringSection().forEach(s -> joiner.add("  " + s.toString()));
    if (pool.getStringSection().isEmpty()) joiner.add("  none");
    joiner.add("Type References: ");
    pool.getTypeSection().forEach(t -> joiner.add("  " + t.toString()));
    if (pool.getTypeSection().isEmpty()) joiner.add("  none");
    joiner.add("Field References: ");
    pool.getFieldSection().forEach(f -> joiner.add("  " + f.toString()));
    if (pool.getFieldSection().isEmpty()) joiner.add("  none");
    joiner.add("Method References: ");
    pool.getMethodSection().forEach(m -> joiner.add("  " + m.toString()));
    if (pool.getMethodSection().isEmpty()) joiner.add("  none");
    return joiner.toString();
  }

  private boolean validateArgs() {
    if (files == null || files.isEmpty()) {
      System.err.println("\n  Please provide input files\n");
      return false;
    }
    if (!VALID_ITEM_TYPES.contains(itemType)) {
      System.err.println("\n  Please enter correct item type\n");
      return false;
    }
    if (VALID_REFERENCE_TYPES.stream().noneMatch(refType::contains)) {
      System.err.println("\n  Please enter correct reference type\n");
      return false;
    }
    if (references.isEmpty()) {
      System.err.println("\n  Please provide references\n");
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
