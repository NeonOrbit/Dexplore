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
 * <p>
 * <b>Note:</b> The filter matches only if all the specified conditions are satisfied.
 * <p>
 * Use the {@link Builder Builder} class to create filter instances.
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
            (sourceNames == null || containsSourceFileName(dexClass.getSourceFile())) &&
            (superClass == null || superClass.equals(dexClass.getSuperclass())) &&
            (pkgPattern == null || pkgPattern.matcher(dexClass.getType()).matches()) &&
            (interfaces == null || dexClass.getInterfaces().equals(interfaces)) &&
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
   * Creates an instance that matches a single class with the specified class name.
   * <p>
   *   <b>Reminder:</b> Inner class names are separated by a {@code $} sign, not a {@code dot}.
   *   <br>Such as: {@code app.pkg.OuterClassName$InnerClassName}
   * </p>
   * @param clazz {@linkplain Class#getName() full name} of the desired class
   * @return a {@code ClassFilter} instance that matches the specified class
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

  /**
   * Builder for creating {@code ClassFilter} instances.
   * <p>
   * <b>Note:</b> The filter matches only if all the specified conditions are satisfied.
   * <p>Example:
   * <pre>{@code
   *  ClassFilter.builder()
   *      .setReferenceTypes(ReferenceTypes.STRINGS_ONLY)
   *      .setReferenceFilter(pool -> pool.contains("..."))
   *      .setModifiers(Modifier.PUBLIC)
   *      .......
   *      .build()
   *  ...
   * }</pre>
   */
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
     * Set a condition to match classes only from the given packages.
     * <p>
     *   <b>Note:</b> This silently overwrites {@link #skipPackages(List, List) skipPackages()}.
     *   <br><b>Remark:</b> This condition uses a regex pattern internally.
     * </p>
     * @param packages package names
     * @return {@code this} builder
     * @see #skipPackages(List, List) skipPackages()
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
     *   <b>Note:</b> This silently overwrites {@link #setPackages(String...) setPackages()}.
     *   <br><b>Remark:</b> This condition uses regex pattern internally.
     * </p>
     * @param packages a list of packages to exclude
     * @param exception an exception list to allow sub packages (can be null)
     * @return {@code this} builder
     * @see #setPackages(String...)
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
     * Set a regex pattern for filtering classes by matching against their {@linkplain Class#getName() full names}.
     * @param regex pattern to match against classes
     * @return {@code this} builder
     */
    public Builder setClassPattern(@Nullable Pattern regex) {
      this.clsPattern = regex == null ? null : DexUtils.javaToDexPattern(regex);
      return this;
    }

    /**
     * Set a condition to match only the classes from the specified source files.
     * <p> Example: <pre>{@code setSourceNames("Application.java", "FileName.java", ...)}</pre>
     * @param sources source file names
     * @return {@code this} builder
     */
    public Builder setSourceNames(@Nonnull String... sources) {
      List<String> list = Utils.nonNullList(sources);
      this.sourceNames = list.isEmpty() ? null : Utils.optimizedSet(list);
      return this;
    }

    /**
     * Set a condition to match only the classes specified by the given class names.
     * <p>
     * <b>Note:</b> This method accepts the {@linkplain Class#getName() full} names of classes.
     * If you prefer to match using the {@linkplain Class#getSimpleName() simple} names of classes,
     * please use the {@link #setClassSimpleNames(String...) setClassSimpleNames()} method instead.
     * <p>
     * <b>Reminder:</b> Inner class names are separated by a dollar {@code $} sign, not a {@code dot}.
     * <br>Such as: {@code app.pkg.OuterClassName$InnerClassName}
     *
     * @param classes {@linkplain Class#getName() full names} of the classes
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
     * Set a condition to match only the classes that match any of the specified simple names.
     * <p>
     *   <b>Note:</b> This method accepts the {@linkplain Class#getSimpleName() simple} names of classes.
     *   This is different from the {@link #setClasses(String...) setClasses()} method,
     *   which accepts the full names of classes instead.
     * </p>
     * @param names {@linkplain Class#getSimpleName() simple} names of the classes
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
     * <p> <b>Default:</b> disabled
     * @param enable {@code true} to enable, {@code false} to disable
     * @return {@code this} builder
     * @see #enableSyntheticMembers(boolean)
     */
    public Builder enableSyntheticClasses(boolean enable) {
      this.synthClass = enable;
      return this;
    }

    /**
     * Specify whether the synthetic items of the class should also be checked.
     * <p>Synthetic items are
     * {@link #setNumbers(Number...) numbers},
     * {@link #setReferenceFilter(ReferenceFilter) references},
     * {@link #containsAnnotations(String...) annotations},
     * {@link #containsAnnotationValues(String...) annotation-values}
     * of synthetic members (synthetic fields and methods).
     * <p>
     *   <b>Note:</b> This also includes the synthetic members
     *   and their references in the resulting {@code ClassData} objects.
     * </p>
     * <b>Default:</b> disabled
     * @param enable {@code true} to enable, {@code false} to disable
     * @return {@code this} builder
     * @see #enableSyntheticClasses(boolean)
     */
    public Builder enableSyntheticMembers(boolean enable) {
      this.synthItems = enable;
      return this;
    }

    /**
     * Set a condition to match only the classes with the specified class {@linkplain Class#getModifiers() modifiers}.
     * <blockquote>Examples:
     *    <pre> setModifiers({@linkplain Modifier#PUBLIC}) </pre>
     *    <pre> setModifiers({@linkplain Modifier#PUBLIC} | {@linkplain Modifier#FINAL}) </pre>
     * </blockquote>
     * @param modifiers class {@link Class#getModifiers() modifiers}, or -1 to unset
     * @return {@code this} builder
     * @see #skipModifiers(int)
     */
    public Builder setModifiers(int modifiers) {
      this.flag = modifiers;
      return this;
    }

    /**
     * Classes matching the specified {@linkplain Class#getModifiers() modifiers} are omitted from the search process.
     * @param modifiers class {@linkplain Class#getModifiers() modifiers}, or -1 to unset
     * @return {@code this} builder
     * @see #setModifiers(int)
     */
    public Builder skipModifiers(int modifiers) {
      this.skipFlag = modifiers;
      return this;
    }

    /**
     * Set a condition to match only the classes with the specified superclass.
     * @param superclass {@linkplain Class#getName() full name} of the superclass
     * @return {@code this} builder
     * @see #defaultSuperClass()
     */
    public Builder setSuperClass(@Nullable String superclass) {
      this.superClass = superclass == null ? null : DexUtils.javaToDexTypeName(superclass);
      return this;
    }

    /**
     * Set a condition to match only the classes that do not have any explicit superclass.
     * <p>This is equivalent to calling: <pre> {@code setSuperClass(Object.class.getName())} </pre>
     * @return {@code this} builder
     * @see #setSuperClass(String)
     */
    public Builder defaultSuperClass() {
      this.superClass = DexUtils.javaClassToDexTypeName(Object.class);
      return this;
    }

    /**
     * Set a condition to match only the classes with the specified interfaces.
     * <p>
     *   <b>Note:</b> The interface list must precisely match in the correct order.
     *   An empty list exclusively matches classes with no interfaces.
     * </p>
     * @param interfaces an ordered list of the interfaces ({@linkplain Class#getName() full names})
     * @return {@code this} builder
     * @see #noInterfaces()
     */
    public Builder setInterfaces(@Nullable List<String> interfaces) {
      this.interfaces = interfaces == null ? null : Utils.optimizedList(DexUtils.javaToDexTypeName(interfaces));
      return this;
    }

    /**
     * Set a condition to match only the classes that do not have any interfaces.
     * <p> This is equivalent to calling: <pre> {@code setInterfaces(Collections.emptyList())} </pre>
     * @return {@code this} builder
     * @see #setInterfaces(List)
     */
    public Builder noInterfaces() {
      return setInterfaces(Collections.emptyList());
    }

    /**
     * Set a condition to match only the classes that contain all the specified annotations.
     * <p>
     *   <b>Note:</b> This also checks the annotations from class members.
     * </p>
     * @param annotations {@linkplain Class#getName() full names} of the annotation types
     * @return {@code this} builder
     * @see #containsAnnotationValues(String...)
     */
    public Builder containsAnnotations(@Nonnull String... annotations) {
      List<String> list = DexUtils.javaToDexTypeName(Utils.nonNullList(annotations));
      this.annotations = list.isEmpty() ? null : Utils.optimizedSet(list);
      return this;
    }

    /**
     * Set a condition to match only the classes that contain all the specified annotation values.
     * <p>
     * <b>Note:</b> This also checks the annotation values from class members.
     * <p>
     * <b>Note:</b> Currently, only String and Type values are supported.
     * <p>
     * String values: <pre>{@code @Annotation("apple") @Annotation({"banana", "cherry"})}</pre>
     * Filter example: <pre>{@code builder.containsAnnotationValues("apple", "banana", "cherry")}</pre>
     * Type values: <pre>{@code @Annotation(Runnable.class), @Annotation(Thread.class)}</pre>
     * Filter example: <pre>{@code builder.containsAnnotationValues("java.lang.Runnable", Thread.class.getName())}</pre>
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
     * Set a condition to match only the classes that contain all the specified numbers.
     * <p> <b>Note:</b> Each float value must end with an {@code f} character.
     * @param numbers list of numbers
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
