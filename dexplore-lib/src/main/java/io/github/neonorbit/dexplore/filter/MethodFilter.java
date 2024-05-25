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
import org.jf.dexlib2.dexbacked.DexBackedMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A filter used to select dex methods of interest.
 * <p>
 * <b>Note:</b> The filter matches only if all the specified conditions are satisfied.
 * <p>
 * Use the {@link Builder Builder} class to create filter instances.
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public final class MethodFilter extends BaseFilter<DexBackedMethod> {
  private static final int NEG = -1;

  /** A {@code MethodFilter} instance that matches all dex methods. */
  public static final MethodFilter MATCH_ALL = new MethodFilter(builder());

  private final int flag;
  private final int skipFlag;
  private final int paramSize;
  private final boolean synthetic;
  private final String returnType;
  private final Set<String> methodNames;
  private final List<String> parameters;
  private final Set<String> annotations;
  private final Set<String> annotValues;
  private final Set<Long> numLiterals;

  private MethodFilter(Builder builder) {
    super(builder, isUniqueSig(builder));
    this.flag = builder.flag;
    this.skipFlag = builder.skipFlag;
    this.synthetic = builder.synthetic;
    this.paramSize = builder.paramSize;
    this.parameters = builder.parameters;
    this.returnType = builder.returnType;
    this.methodNames = builder.methodNames;
    this.annotations = builder.annotations;
    this.annotValues = builder.annotValues;
    this.numLiterals = builder.numLiterals;
  }

  public boolean synthEnabled() {
    return synthetic;
  }

  private static boolean isUniqueSig(Builder b) {
    if (!Utils.isSingle(b.methodNames)) return false;
    return (b.parameters != null || b.paramSize == 0) && (!b.synthetic || b.returnType != null);
  }

  @Internal
  @Override
  public boolean verify(@Nonnull DexBackedMethod dexMethod,
                        @Nonnull LazyDecoder<DexBackedMethod> decoder) {
    if (this == MATCH_ALL) return true;
    if (DexUtils.skipSynthetic(synthetic, dexMethod.accessFlags)) return false;
    if (!checkMethodSignature(dexMethod)) return false;
    boolean result = (
            (flag == NEG || (dexMethod.accessFlags & flag) == flag) &&
            (skipFlag == NEG || (dexMethod.accessFlags & skipFlag) == 0) &&
            (returnType == null || returnType.equals(dexMethod.getReturnType())) &&
            (annotations == null || FilterUtils.containsAllAnnotations(dexMethod, annotations)) &&
            (annotValues == null || FilterUtils.containsAllAnnotationValues(dexMethod, annotValues)) &&
            (numLiterals == null || DexDecoder.decodeNumberLiterals(dexMethod).containsAll(numLiterals)) &&
            super.verify(dexMethod, decoder)
    );
    if (unique && !result) {
      throw new AbortException("Method found but the filter didn't match");
    }
    return result;
  }

  private boolean checkMethodSignature(DexBackedMethod dexMethod) {
    if (methodNames != null && !methodNames.contains(dexMethod.getName())) {
      return false;
    } else if (parameters != null) {
      List<String> list = dexMethod.getParameterTypes();
      return parameters.size() == list.size() && list.equals(parameters);
    }
    return paramSize < 0 || dexMethod.getParameterTypes().size() == paramSize;
  }

  /**
   * Creates an instance that matches a single method with the specified method name and no parameters.
   * @param method name of the desired method
   * @return a {@code MethodFilter} instance that matches the specified method
   * @see #ofMethod(String, List) ofMethod(method, params)
   */
  public static MethodFilter ofMethod(@Nonnull String method) {
    return ofMethod(method, Collections.emptyList());
  }

  /**
   * Creates an instance that matches a single method with the specified method name and parameters.
   * @param method name of the desired method
   * @param params an ordered list of the parameters
   * @return a {@code MethodFilter} instance that matches the specified method
   * @see #ofMethod(String) ofMethod(method)
   */
  public static MethodFilter ofMethod(@Nonnull String method,
                                      @Nonnull List<String> params) {
    return builder().setMethodNames(method).setParamList(params).build();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder for creating {@code MethodFilter} instances.
   * <p>
   * <b>Note:</b> The filter matches only if all the specified conditions are satisfied.
   * <p>Example:
   * <pre>{@code
   *  MethodFilter.builder()
   *      .setReferenceTypes(ReferenceTypes.STRINGS_ONLY)
   *      .setReferenceFilter(pool -> pool.contains("..."))
   *      .setModifiers(Modifier.PUBLIC)
   *      .......
   *      .build()
   *  ...
   * }</pre>
   */
  public static class Builder extends BaseFilter.Builder<Builder, MethodFilter> {
    private int flag = NEG;
    private int skipFlag = NEG;
    private boolean synthetic;
    private int paramSize = NEG;
    private String returnType;
    private Set<String> methodNames;
    private List<String> parameters;
    private Set<String> annotations;
    private Set<String> annotValues;
    private Set<Long> numLiterals;

    public Builder() {}

    private Builder(MethodFilter instance) {
      super(instance);
      this.flag = instance.flag;
      this.skipFlag = instance.skipFlag;
      this.synthetic = instance.synthetic;
      this.paramSize = instance.paramSize;
      this.parameters = instance.parameters;
      this.returnType = instance.returnType;
      this.methodNames = instance.methodNames;
      this.annotations = instance.annotations;
      this.annotValues = instance.annotValues;
      this.numLiterals = instance.numLiterals;
    }

    @Override
    protected boolean isDefault() {
      return super.isDefault()    &&
              !synthetic          &&
              flag        == NEG  &&
              skipFlag    == NEG  &&
              paramSize   == NEG  &&
              parameters  == null &&
              returnType  == null &&
              methodNames == null &&
              annotations == null &&
              annotValues == null &&
              numLiterals == null;
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    @Override
    public MethodFilter build() {
      return isDefault() ? MATCH_ALL : new MethodFilter(this);
    }

    /**
     * Set a condition to match only the methods specified by the given method names.
     * @param names method names
     * @return {@code this} builder
     */
    public Builder setMethodNames(@Nonnull String... names) {
      List<String> list = Utils.nonNullList(names);
      this.methodNames = list.isEmpty() ? null : Utils.optimizedSet(list);
      return this;
    }

    /**
     * Specify whether to include synthetic methods in the search.
     * <p>
     * <b>Note:</b> This does not cover methods from synthetic classes unless they are also enabled
     * in {@link ClassFilter.Builder#enableSyntheticClasses(boolean) ClassFilter.enableSyntheticClasses()}.
     * <p>
     * <b>Default:</b> disabled
     * @param enable {@code true} to enable, {@code false} to disable
     * @return {@code this} builder
     * @see ClassFilter.Builder#enableSyntheticClasses(boolean) ClassFilter.enableSyntheticClasses()
     */
    public Builder enableSyntheticMethods(boolean enable) {
      this.synthetic = enable;
      return this;
    }

    /**
     * Set a condition to match only the methods with the specified method {@linkplain Method#getModifiers() modifiers}.
     * <blockquote>Examples:
     *    <pre> setModifiers({@linkplain Modifier#PUBLIC}) </pre>
     *    <pre> setModifiers({@linkplain Modifier#PUBLIC} | {@linkplain Modifier#FINAL}) </pre>
     * </blockquote>
     * @param modifiers method {@linkplain Method#getModifiers() modifiers}, or -1 to unset
     * @return {@code this} builder
     * @see #skipModifiers(int)
     */
    public Builder setModifiers(int modifiers) {
      this.flag = modifiers;
      return this;
    }

    /**
     * Methods matching the specified {@linkplain Method#getModifiers() modifiers} are omitted from the search process.
     * @param modifiers method {@linkplain Method#getModifiers() modifiers}, or -1 to unset
     * @return {@code this} builder
     * @see #setModifiers(int)
     */
    public Builder skipModifiers(int modifiers) {
      this.skipFlag = modifiers;
      return this;
    }

    /**
     * Set a condition to match only the methods with the specified method return type.
     * @param returnType {@linkplain Class#getName() full name} of the return type
     * @return {@code this} builder
     */
    public Builder setReturnType(@Nullable String returnType) {
      this.returnType = returnType == null ? null : DexUtils.javaToDexTypeName(returnType);
      return this;
    }

    /**
     * Set a condition to match only the methods with the specified number of method parameters.
     * @param size number of method parameters, or -1 to unset
     * @return {@code this} builder
     */
    public Builder setParamSize(int size) {
      this.paramSize = size;
      return this;
    }

    /**
     * Set a condition to match only the methods with the specified parameters.
     * <p>
     *   <b>Note:</b> The parameter list must precisely match in the correct order.
     *   An empty list match only the methods with no parameters.
     * </p>
     * @param params an ordered list of the parameter types ({@linkplain Class#getName() full names})
     * @return {@code this} builder
     */
    public Builder setParamList(@Nullable List<String> params) {
      this.parameters = params == null ? null : Utils.optimizedList(DexUtils.javaToDexTypeName(params));
      return this;
    }

    /**
     * Set a condition to match only the methods that contain all the specified annotations.
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
     * Set a condition to match only the methods that contain all the specified annotation values.
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
     * Set a condition to match only the methods that contain all the specified numbers.
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
  }
}
