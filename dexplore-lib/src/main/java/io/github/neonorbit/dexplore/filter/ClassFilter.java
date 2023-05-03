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

import io.github.neonorbit.dexplore.LazyDecoder;
import io.github.neonorbit.dexplore.exception.AbortException;
import io.github.neonorbit.dexplore.util.DexUtils;
import io.github.neonorbit.dexplore.iface.Internal;
import io.github.neonorbit.dexplore.util.Utils;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.security.InvalidParameterException;
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
  }

  @Internal
  @Override
  public boolean verify(@Nonnull DexBackedClassDef dexClass,
                        @Nonnull LazyDecoder<DexBackedClassDef> decoder) {
    if (this == MATCH_ALL) return true;
    if (classNames != null && !classNames.contains(dexClass.getType())) return false;
    boolean result = (
            (flag == M1 || (dexClass.getAccessFlags() & flag) == flag) &&
            (skipFlag == M1 || (dexClass.getAccessFlags() & skipFlag) == 0) &&
            (superClass == null || superClass.equals(dexClass.getSuperclass())) &&
            (pkgPattern == null || pkgPattern.matcher(dexClass.getType()).matches()) &&
            (interfaces == null || dexClass.getInterfaces().equals(interfaces)) &&
            (sourceNames == null || sourceNames.contains(Utils.getString(dexClass.getSourceFile()))) &&
            (annotations == null || FilterUtils.containsAnnotations(dexClass, annotations)) &&
            (annotValues == null || FilterUtils.containsAnnotationValues(dexClass, annotValues)) &&
            super.verify(dexClass, decoder)
    );
    if (unique && !result) {
      throw new AbortException("Class found but the filter didn't match");
    }
    return result;
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
              annotValues == null;
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
     * Specify a list of packages that should be excluded.
     *
     * @param packages a list of packages to exclude
     * @param exception an exception list to allow sub packages
     * @return {@code this} builder
     */
    public Builder skipPackages(@Nullable List<String> packages,
                                @Nullable List<String> exception) {
      if (packages == null || packages.isEmpty()) {
        this.pkgPattern = null;
      } else if (!Utils.isValidName(packages) ||
                 !(exception == null || Utils.isValidName(exception))) {
        throw new InvalidParameterException("Invalid Package Name");
      } else {
        this.pkgPattern = getPackagePattern(packages, exception);
      }
      return this;
    }

    /**
     * Add a condition to the filter to match classes that match with any of the specified classes.
     * <p>This is useful if you want to search in specific classes only.</p>
     *
     * @param classes {@linkplain Class#getName() full names} of classes
     * @return {@code this} builder
     */
    public Builder setClasses(@Nonnull String... classes) {
      List<String> list = DexUtils.javaToDexTypeName(Utils.nonNullList(classes));
      this.classNames = list.isEmpty() ? null : Utils.optimizedSet(list);
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
     * @param modifiers class {@link Class#getModifiers() modifiers}
     * @return {@code this} builder
     */
    public Builder setModifiers(int modifiers) {
      this.flag = modifiers;
      return this;
    }

    /**
     * Classes with the specified class modifiers will be skipped.
     *
     * @param modifiers class {@link Class#getModifiers() modifiers}
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
     * Add a condition to the filter to match classes that contains all the specified annotations.
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
     * Add a condition to the filter to match classes that contains
     * all the specified annotation values.
     *
     * <p>Currently supports only string and type values.</p>
     * <pre>
     *   STRING Values: @Annot("string"), or @Annot({"string1", "string2"})
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

    private static Pattern getPackagePattern(List<String> packages, List<String> exception) {
      Function<String, String> mapper = s -> 'L' + s.replaceAll("\\.", "/")
                                                    .replaceAll("\\$", "\\\\\\$")
                                           + '/';
      String regex = "^((?!";
      regex += packages.stream().map(mapper).collect(Collectors.joining("|"));
      regex += ")";
      if (exception != null && !exception.isEmpty()) {
        regex += '|' + exception.stream().map(mapper).collect(Collectors.joining("|"));
      }
      regex += ").*$";
      return Pattern.compile(regex);
    }
  }
}
