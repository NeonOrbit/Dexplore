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

/**
 * A filter used to select dex classes of interest.
 * <p><br>
 *   Note: The filter will match if and only if all the specified conditions are satisfied.
 * </p>
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public final class ClassFilter extends BaseFilter<DexBackedClassDef> {
  private static final int M1 = -1;

  /** A {@code ClassFilter} instance that matches all dex classes. */
  public static final ClassFilter MATCH_ALL = new ClassFilter(builder());

  private final int flag;
  private final int skipFlag;
  private final String superClass;
  private final Pattern pkgPattern;
  private final Set<String> classNames;
  private final List<String> interfaces;
  private final Set<String> sourceNames;
  private final Set<String> annotations;
  private final Set<String> annotValues;
  private final Set<Long> numLiterals;
  private final Set<String> clsShortNames;

  private ClassFilter(Builder builder) {
    super(builder, Utils.isSingle(builder.classNames));
    this.flag = builder.flag;
    this.skipFlag = builder.skipFlag;
    this.pkgPattern = builder.pkgPattern;
    this.superClass = builder.superClass;
    this.classNames = builder.classNames;
    this.interfaces = builder.interfaces;
    this.sourceNames = builder.sourceNames;
    this.annotations = builder.annotations;
    this.annotValues = builder.annotValues;
    this.numLiterals = builder.numLiterals;
    this.clsShortNames = builder.clsShortNames;
  }

  @Internal
  @Override
  public boolean verify(@Nonnull DexBackedClassDef dexClass,
                        @Nonnull LazyDecoder<DexBackedClassDef> decoder) {
    if (this == MATCH_ALL) return true;
    if (!checkClassNames(dexClass.getType())) return false;
    boolean result = (
            (flag == M1 || (dexClass.getAccessFlags() & flag) == flag) &&
            (skipFlag == M1 || (dexClass.getAccessFlags() & skipFlag) == 0) &&
            (superClass == null || superClass.equals(dexClass.getSuperclass())) &&
            (pkgPattern == null || pkgPattern.matcher(dexClass.getType()).matches()) &&
            (interfaces == null || dexClass.getInterfaces().equals(interfaces)) &&
            (sourceNames == null || sourceNames.contains(Utils.getString(dexClass.getSourceFile()))) &&
            (annotations == null || FilterUtils.containsAllAnnotations(dexClass, annotations)) &&
            (annotValues == null || FilterUtils.containsAllAnnotationValues(dexClass, annotValues)) &&
            (numLiterals == null || DexDecoder.decodeNumberLiterals(dexClass).containsAll(numLiterals)) &&
            super.verify(dexClass, decoder)
    );
    if (unique && !result) {
      throw new AbortException("Class found but the filter didn't match");
    }
    return result;
  }

  private boolean checkClassNames(String name) {
    if (classNames != null) {
      return classNames.contains(name);
    }
    return clsShortNames == null || clsShortNames.stream().anyMatch(name::endsWith);
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
    Objects.requireNonNull(clazz);
    return builder().setClasses(clazz).build();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder extends BaseFilter.Builder<Builder, ClassFilter> {
    private int flag = M1;
    private int skipFlag = M1;
    private Pattern pkgPattern;
    private String superClass;
    private Set<String> classNames;
    private List<String> interfaces;
    private Set<String> sourceNames;
    private Set<String> annotations;
    private Set<String> annotValues;
    private Set<Long> numLiterals;
    private Set<String> clsShortNames;

    public Builder() {}

    private Builder(ClassFilter instance) {
      super(instance);
      this.flag = instance.flag;
      this.skipFlag = instance.skipFlag;
      this.pkgPattern = instance.pkgPattern;
      this.superClass = instance.superClass;
      this.classNames = instance.classNames;
      this.interfaces = instance.interfaces;
      this.sourceNames = instance.sourceNames;
      this.annotations = instance.annotations;
      this.annotValues = instance.annotValues;
      this.numLiterals = instance.numLiterals;
      this.clsShortNames = instance.clsShortNames;
    }

    @Override
    protected boolean isDefault() {
      return super.isDefault()     &&
              flag        == M1    &&
              skipFlag    == M1    &&
              pkgPattern  == null  &&
              superClass  == null  &&
              classNames  == null  &&
              interfaces  == null  &&
              sourceNames == null  &&
              annotations == null  &&
              annotValues == null  &&
              numLiterals == null  &&
              clsShortNames == null;
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    @Override
    public ClassFilter build() {
      return isDefault() ? MATCH_ALL : new ClassFilter(this);
    }

    /**
     * Add a condition to the filter to match classes from the specified packages only.
     * <p><b>Note:</b>
     *    This will silently overwrite {@link #skipPackages(List, List) skipPackages()}.
     * </p>
     *
     * @param packages package names
     * @return {@code this} builder
     */
    public Builder setPackages(@Nonnull String... packages) {
      List<String> pkg = Utils.nonNullList(packages);
      if (!Utils.hasItem(pkg)) {
        this.pkgPattern = null;
      } else if (!Utils.isValidName(pkg)) {
        throw new IllegalArgumentException("Invalid Package Name");
      } else {
        this.pkgPattern = getPackagePattern(pkg, null);
      }
      return this;
    }

    /**
     * Specify a list of packages that should be excluded.
     * <p><b>Note:</b>
     *    This will silently overwrite {@link #setPackages(String...) setPackages()}.
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
      } else if (!Utils.isValidName(packages) ||
                !(!Utils.hasItem(exception) || Utils.isValidName(exception))) {
        throw new IllegalArgumentException("Invalid Package Name");
      } else {
        this.pkgPattern = getPackagePattern(exception, packages);
      }
      return this;
    }

    /**
     * Add a condition to the filter to match classes that match with any of the specified classes.
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
      if (this.clsShortNames != null) throw new IllegalStateException(
              "ClassFilter: setClasses() cannot be used together with setClassSimpleNames()"
      );
      List<String> list = DexUtils.javaToDexTypeName(Utils.nonNullList(classes));
      this.classNames = list.isEmpty() ? null : Utils.optimizedSet(list);
      return this;
    }

    /**
     * Add a condition to the filter to match classes that match with any of the specified class names.
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
      if (this.classNames != null) throw new IllegalStateException(
              "ClassFilter: setClassSimpleNames() cannot be used together with setClasses()"
      );
      List<String> list = Utils.nonNullList(names).stream().map(n-> n+';').collect(Collectors.toList());
      this.clsShortNames = list.isEmpty() ? null : Utils.optimizedSet(list);
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
      List<String> list = Utils.nonNullList(annotations);
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
     * Add a condition to the filter to match methods that contain all the specified numbers.
     * <p>Note: Each float value must end with an 'f' character.</p>
     *
     * @param numbers list of numbers to match
     * @return {@code this} builder
     */
    public Builder setNumbers(@Nonnull Number... numbers) {
      Set<Long> literals = Utils.nonNullList(numbers).stream().map(number ->
              number instanceof Float ? Float.floatToIntBits((Float) number) :
                      number instanceof Double ? Double.doubleToLongBits((Double) number) :
                              number.longValue()
      ).collect(Collectors.toSet());
      this.numLiterals = literals.isEmpty() ? null : Utils.optimizedSet(literals);
      return this;
    }

    private static Pattern getPackagePattern(List<String> includes, List<String> excludes) {
      Function<String, String> mapper = s -> 'L' + s.replaceAll("\\.", "/")
                                                    .replaceAll("\\$", "\\\\\\$")
                                           + '/';
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
