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

package io.github.neonorbit.dexplore.filter;

import io.github.neonorbit.dexplore.DexDecoder;
import io.github.neonorbit.dexplore.LazyDecoder;
import io.github.neonorbit.dexplore.exception.AbortException;
import io.github.neonorbit.dexplore.iface.Internal;
import io.github.neonorbit.dexplore.util.DexUtils;
import io.github.neonorbit.dexplore.util.Utils;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.github.neonorbit.dexplore.util.Utils.isSingle;
import static io.github.neonorbit.dexplore.util.Utils.isValidName;

/**
 * A filter used to select dex classes of interest.
 * <p><br>
 *   Note: The filter will match only if all the specified conditions are satisfied.
 * </p>
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public final class ClassFilter extends BaseFilter<DexBackedClassDef> {
  private static final int NEG = -1;

  /** A {@code ClassFilter} instance that matches all dex classes. */
  public static final ClassFilter MATCH_ALL = new ClassFilter(builder());

  private final int flag;
  private final int skipFlag;
  private final boolean synthClass;
  private final boolean synthItems;
  private final String superClass;
  private final Pattern pkgPattern;
  private final Pattern clsPattern;
  private final Set<String> classNames;
  private final Set<String> shortNames;
  private final List<String> interfaces;
  private final Set<String> sourceNames;
  private final Set<String> annotations;
  private final Set<String> annotValues;
  private final Set<Long> numLiterals;

  private ClassFilter(Builder builder) {
    super(builder, isSingle(builder.classNames));
    this.flag = builder.flag;
    this.skipFlag = builder.skipFlag;
    this.synthClass = builder.synthClass;
    this.synthItems = builder.synthItems;
    this.superClass = builder.superClass;
    this.pkgPattern = builder.pkgPattern;
    this.clsPattern = builder.clsPattern;
    this.classNames = builder.classNames;
    this.shortNames = builder.shortNames;
    this.interfaces = builder.interfaces;
    this.sourceNames = builder.sourceNames;
    this.annotations = builder.annotations;
    this.annotValues = builder.annotValues;
    this.numLiterals = builder.numLiterals;
  }

  @Internal
  public boolean synthItems() {
    return synthItems;
  }

  @Internal
  @Override
  public boolean verify(@Nonnull DexBackedClassDef dexClass,
                        @Nonnull LazyDecoder<DexBackedClassDef> decoder) {
    if (this == MATCH_ALL) return true;
    int classAccessFlags = dexClass.getAccessFlags();
    if (DexUtils.skipSynthetic(synthClass, classAccessFlags)) return false;
    if (!checkClassNames(dexClass.getType())) return false;
    boolean result = (
            (flag == NEG || (classAccessFlags & flag) == flag) &&
            (skipFlag == NEG || (classAccessFlags & skipFlag) == 0) &&
            (superClass == null || superClass.equals(dexClass.getSuperclass())) &&
            (pkgPattern == null || pkgPattern.matcher(dexClass.getType()).matches()) &&
            (interfaces == null || dexClass.getInterfaces().equals(interfaces)) &&
            (sourceNames == null || containsSourceFileName(dexClass.getSourceFile())) &&
            (annotations == null || FilterUtils.containsAllAnnotations(dexClass, annotations, synthItems)) &&
            (annotValues == null || FilterUtils.containsAllAnnotationValues(dexClass, annotValues, synthItems)) &&
            (numLiterals == null || DexDecoder.decodeNumberLiterals(dexClass, synthItems).containsAll(numLiterals)) &&
            super.verify(dexClass, decoder)
    );
    if (unique && !result) {
      throw new AbortException("Class found but the filter didn't match");
    }
    return result;
  }

  private boolean containsSourceFileName(String source) {
    return source != null && sourceNames.contains(source);
  }

  private boolean checkClassNames(String name) {
    if (clsPattern != null && !clsPattern.matcher(name).matches()) return false;
    if (classNames != null) return classNames.contains(name);
    return shortNames == null || shortNames.stream().anyMatch(shortName -> {
      if (name.length() < shortName.length()) return false;
      if (name.length() == shortName.length()) return name.equals(shortName);
      char separator = name.charAt(name.length() - shortName.length() - 1);
      return (separator == '/' || separator == '$') && name.endsWith(shortName);
    });
  }

  /**
   * This is equivalent to:
   * <blockquote><pre>
   *   new ClassFilter.Builder()
   *                  .{@link Builder#setClasses(String...)
   *                          setClasses(clazz)}
   *                  .build();
   * </pre></blockquote>
   *
   * @param clazz class name
   * @return a {@code ClassFilter} instance
   */
  public static ClassFilter ofClass(@Nonnull String clazz) {
    return builder().setClasses(Objects.requireNonNull(clazz)).build();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder extends BaseFilter.Builder<Builder, ClassFilter> {
    private int flag = NEG;
    private int skipFlag = NEG;
    private boolean synthClass;
    private boolean synthItems;
    private String superClass;
    private Pattern pkgPattern;
    private Pattern clsPattern;
    private Set<String> classNames;
    private List<String> interfaces;
    private Set<String> sourceNames;
    private Set<String> annotations;
    private Set<String> annotValues;
    private Set<Long> numLiterals;
    private Set<String> shortNames;

    public Builder() {}

    private Builder(ClassFilter instance) {
      super(instance);
      this.flag = instance.flag;
      this.skipFlag = instance.skipFlag;
      this.synthClass = instance.synthClass;
      this.synthItems = instance.synthItems;
      this.superClass = instance.superClass;
      this.pkgPattern = instance.pkgPattern;
      this.clsPattern = instance.clsPattern;
      this.classNames = instance.classNames;
      this.shortNames = instance.shortNames;
      this.interfaces = instance.interfaces;
      this.sourceNames = instance.sourceNames;
      this.annotations = instance.annotations;
      this.annotValues = instance.annotValues;
      this.numLiterals = instance.numLiterals;
    }

    @Override
    protected boolean isDefault() {
      return super.isDefault() &&
              !synthClass &&
              !synthItems &&
              flag        == NEG  &&
              skipFlag    == NEG  &&
              superClass  == null &&
              pkgPattern  == null &&
              clsPattern  == null &&
              classNames  == null &&
              shortNames  == null &&
              interfaces  == null &&
              sourceNames == null &&
              annotations == null &&
              annotValues == null &&
              numLiterals == null;
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    @Override
    public ClassFilter build() {
      if (isDefault()) return MATCH_ALL;
      if (types != null) {
        setReferenceTypes(types.withSynthetic(synthItems));
      }
      return new ClassFilter(this);
    }

    /**
     * Add a condition to the filter to match classes from the specified packages only.
     * <p>
     *   <b>Note:</b> This will silently overwrite {@link #skipPackages(List, List) skipPackages()}.
     *   <br>
     *   <b>Remark:</b> This condition uses regex pattern internally.
     * </p>
     *
     * @param packages package names
     * @return {@code this} builder
     */
    public Builder setPackages(@Nonnull String... packages) {
      List<String> pkg = Utils.nonNullList(packages);
      if (!Utils.hasItem(pkg)) {
        this.pkgPattern = null;
      } else if (!isValidName(pkg)) {
        throw new IllegalArgumentException("Invalid Package Name");
      } else {
        this.pkgPattern = getPackagePattern(pkg, null);
      }
      return this;
    }

    /**
     * Specify a list of packages that should be excluded.
     * <p>
     *   <b>Note:</b> This will silently overwrite {@link #setPackages(String...) setPackages()}.
     *   <br>
     *   <b>Remark:</b> This condition uses regex pattern internally.
     * </p>
     *
     * @param packages a list of packages to exclude
     * @param exception an exception list to allow sub packages (can be null)
     * @return {@code this} builder
     */
    public Builder skipPackages(@Nullable List<String> packages,
                                @Nullable List<String> exception) {
      if (!Utils.hasItem(packages)) {
        this.pkgPattern = null;
      } else if (!isValidName(packages) || Utils.hasItem(exception) && !isValidName(exception)) {
        throw new IllegalArgumentException("Invalid Package Name");
      } else {
        this.pkgPattern = getPackagePattern(exception, packages);
      }
      return this;
    }

    /**
     * Add a regex pattern for filtering classes by their full names.
     * <p><b>Note:</b>
     * The pattern will be matched against the {@linkplain Class#getName() full names} of classes.
     * </p>
     *
     * @param regex pattern to match against classes
     * @return {@code this} builder
     */
    public Builder setClassPattern(@Nullable Pattern regex) {
      this.clsPattern = regex == null ? null : DexUtils.javaToDexPattern(regex);
      return this;
    }

    /**
     * Add a condition to the filter to match classes that match with any of the specified class names.
     * <p>
     *   <b>Note:</b> This method takes the {@linkplain Class#getName() full names} of classes
     *   <i>(eg: java.io.FileReader)</i>.
     *   If you want to search with simple class names (eg: FileReader),
     *   use {@link #setClassSimpleNames(String...) setClassSimpleNames()} instead.
     * </p>
     *
     * @param classes {@linkplain Class#getName() full names} of classes
     * @return {@code this} builder
     * @see #setClassSimpleNames(String...) setClassSimpleNames(names)
     */
    public Builder setClasses(@Nonnull String... classes) {
      List<String> list = DexUtils.javaToDexTypeName(Utils.nonNullList(classes));
      this.classNames = list.isEmpty() ? null : Utils.optimizedSet(list);
      if (this.classNames != null && this.shortNames != null) throw new IllegalStateException(
              "ClassFilter: setClasses() cannot be used together with setClassSimpleNames()"
      );
      return this;
    }

    /**
     * Add a condition to the filter to match classes that match with any of the specified class simple-names.
     * <p>
     *   <b>Note:</b> This method takes the {@link Class#getSimpleName() Simple Names} of classes.
     *   This is different from {@link #setClasses(String...) setClasses()},
     *   which takes the full names instead.
     * </p>
     *
     * @param names simple names of classes
     * @return {@code this} builder
     * @see #setClasses(String...) setClasses(classes)
     */
    public Builder setClassSimpleNames(@Nonnull String... names) {
      List<String> list = Utils.nonNullList(names);
      if (list.stream().anyMatch(n -> n.contains(".") || n.contains("/"))) {
        throw new IllegalArgumentException("Invalid class simple-name");
      }
      list = list.stream().map(n -> n + ';').collect(Collectors.toList());
      this.shortNames = list.isEmpty() ? null : Utils.optimizedSet(list);
      if (this.shortNames != null && this.classNames != null) throw new IllegalStateException(
              "ClassFilter: setClassSimpleNames() cannot be used together with setClasses()"
      );
      return this;
    }

    /**
     * Specify whether to include synthetic classes in the search.
     * <br> <b>Default:</b> disabled
     * @param enable {@code true} to enable, {@code false} to disable
     * @return {@code this} builder
     */
    public Builder enableSyntheticClasses(boolean enable) {
      this.synthClass = enable;
      return this;
    }

    /**
     * Specify whether the synthetic items of the class should also be checked.
     * <p>
     * Synthetic items are
     * {@link #setReferenceFilter(ReferenceFilter) references},
     * {@link #setNumbers(Number...) numbers},
     * {@link #containsAnnotations(String...) annotations},
     * {@link #containsAnnotationValues(String...) annotation-values}
     * of synthetic members (synthetic fields and methods).
     * <p>
     *   <b>Note:</b> This will also include synthetic members (synthetic fields and methods)
     *   and their references in the resulting {@code ClassData} object.
     * </p>
     * <b>Default:</b> disabled <br>
     * @param enable {@code true} to enable, {@code false} to disable
     * @return {@code this} builder
     */
    public Builder enableSyntheticMembers(boolean enable) {
      this.synthItems = enable;
      return this;
    }

    /**
     * Add a condition to the filter to match classes with the specified class modifiers.
     * <br>
     * Examples:
     *    <blockquote> setModifiers({@link Modifier#PUBLIC}) </blockquote>
     * Use {@code |} operator to set multiple modifiers:
     *    <blockquote> setModifiers({@link Modifier#PUBLIC} | {@link Modifier#FINAL}) </blockquote>
     *
     * @param modifiers class {@link Class#getModifiers() modifiers}, or -1 to reset
     * @return {@code this} builder
     * @see #skipModifiers(int)
     */
    public Builder setModifiers(int modifiers) {
      this.flag = modifiers;
      return this;
    }

    /**
     * Classes with the specified class modifiers will be skipped.
     *
     * @param modifiers class {@link Class#getModifiers() modifiers}, or -1 to reset
     * @return {@code this} builder
     * @see #setModifiers(int)
     */
    public Builder skipModifiers(int modifiers) {
      this.skipFlag = modifiers;
      return this;
    }

    /**
     * Add a condition to the filter to match classes with the specified superclass.
     *
     * @param superclass {@linkplain Class#getName() full name} of a superclass
     * @return {@code this} builder
     */
    public Builder setSuperClass(@Nullable String superclass) {
      this.superClass = superclass == null ? null : DexUtils.javaToDexTypeName(superclass);
      return this;
    }

    /**
     * This is equivalent to calling:
     * <blockquote> {@code setSuperClass(Object.class.getName())} </blockquote>
     *
     * @return {@code this} builder
     * @see #setSuperClass(String)
     */
    public Builder defaultSuperClass() {
      this.superClass = DexUtils.javaClassToDexTypeName(Object.class);
      return this;
    }

    /**
     * Add a condition to the filter to match classes with the specified interfaces.
     * <p>Note: Interface list must match exactly.</p>
     *
     * @param iFaces {@linkplain Class#getName() full names} of interfaces
     * @return {@code this} builder
     * @see #noInterfaces()
     */
    public Builder setInterfaces(@Nullable List<String> iFaces) {
      this.interfaces = iFaces == null ? null : Utils.optimizedList(DexUtils.javaToDexTypeName(iFaces));
      return this;
    }

    /**
     * Add a condition to the filter to match classes that do not have any interfaces.
     *
     * @return {@code this} builder
     * @see #setInterfaces(List)
     */
    public Builder noInterfaces() {
      return setInterfaces(Collections.emptyList());
    }

    /**
     * Add a condition to the filter to match classes from the specified source files.
     * <p>Examples: "Application.java", "AnyFileName.java" etc.</p>
     *
     * @param sources source file names
     * @return {@code this} builder
     */
    public Builder setSourceNames(@Nonnull String... sources) {
      List<String> list = Utils.nonNullList(sources);
      this.sourceNames = list.isEmpty() ? null : Utils.optimizedSet(list);
      return this;
    }

    /**
     * Add a condition to the filter to match classes that contain all the specified annotations.
     *
     * @param annotations {@linkplain Class#getName() full names} of annotation classes
     * @return {@code this} builder
     * @see #containsAnnotationValues(String...)
     */
    public Builder containsAnnotations(@Nonnull String... annotations) {
      List<String> list = DexUtils.javaToDexTypeName(Utils.nonNullList(annotations));
      this.annotations = list.isEmpty() ? null : Utils.optimizedSet(list);
      return this;
    }

    /**
     * Add a condition to the filter to match classes that contain all the specified annotation values.
     *
     * <p>Currently supports only string and type values.</p>
     * <pre>
     *   STRING Values: @SomeAnnot("string") @AnotherAnnot({"string1", "string2"})
     *   Example: filter.containsAnnotationValues("string", "string1")
     *
     *   TYPE Values: @Annot(Runnable.class), @Annot(Thread.class)
     *   Example: filter.containsAnnotationValues("java.lang.Runnable", "java.lang.Thread")
     * </pre>
     *
     * @param annotationValues annotation values
     * @return {@code this} builder
     * @see #containsAnnotations(String...)
     */
    public Builder containsAnnotationValues(@Nonnull String... annotationValues) {
      List<String> list = Utils.nonNullList(annotationValues);
      this.annotValues = list.isEmpty() ? null : Utils.optimizedSet(list);
      return this;
    }

    /**
     * Add a condition to the filter to match classes that contain all the specified numbers.
     * <p>Note: Each float value must end with an 'f' character.</p>
     *
     * @param numbers list of numbers to match
     * @return {@code this} builder
     */
    public Builder setNumbers(@Nonnull Number... numbers) {
      Set<Long> literals = Utils.nonNullList(numbers).stream().map(number ->
              number instanceof Float ? Float.floatToRawIntBits((Float) number) :
                      number instanceof Double ? Double.doubleToRawLongBits((Double) number) :
                              number.longValue()
      ).collect(Collectors.toSet());
      this.numLiterals = literals.isEmpty() ? null : Utils.optimizedSet(literals);
      return this;
    }

    private static Pattern getPackagePattern(List<String> includes, List<String> excludes) {
      Function<String, String> mapper = s -> 'L' + s.replaceAll("\\.", "/") + '/';
      String regex = "^(";
      if (Utils.hasItem(includes)) {
        regex += includes.stream().map(mapper).collect(Collectors.joining("|"));
      }
      if (Utils.hasItem(excludes)) {
        boolean multiple = regex.length() > 2;
        if (multiple) regex += "|(";
        regex += "?!" + excludes.stream().map(mapper).collect(Collectors.joining("|"));
        if (multiple) regex += ")";
      }
      regex += ").*$";
      return Pattern.compile(regex);
    }
  }
}
