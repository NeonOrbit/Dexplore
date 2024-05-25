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

package io.github.neonorbit.dexplore.reference;

import io.github.neonorbit.dexplore.ReferencePool;
import io.github.neonorbit.dexplore.result.MethodData;
import io.github.neonorbit.dexplore.util.DexUtils;
import io.github.neonorbit.dexplore.util.Utils;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * This class represents a {@linkplain io.github.neonorbit.dexplore.reference reference} to a method identifier.
 * <p>
 * Constant values:
 * <ul>
 *   <li>{@link #getDeclaringClass() class} - the declaring class of the method.</li>
 *   <li>{@link #getName() name} - the name of the method.</li>
 *   <li>{@link #getParameterTypes() params} - the parameters of the method.</li>
 *   <li>{@link #getReturnType() return} - the return type of the method.</li>
 * </ul>
 *
 * @see ReferencePool
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public final class MethodRefData implements DexRefData {
  private final boolean details;
  private boolean resolved;
  private MethodReference data;

  private MethodRefData(MethodReference reference, boolean details) {
    this.details = details;
    this.data = reference;
  }

  public static MethodRefData build(MethodReference reference, boolean details) {
    return new MethodRefData(reference, details);
  }

  private MethodReference getData() {
    if (!resolved) {
      resolved = true;
      data = new ImmutableMethodReference(
              details ? DexUtils.dexToJavaTypeName(data.getDefiningClass()) : "",
              data.getName(),
              details ? DexUtils.dexToJavaTypeName(Utils.toStringList(data.getParameterTypes())) : null,
              details ? DexUtils.dexToJavaTypeName(data.getReturnType()) : ""
      );
    }
    return data;
  }

  /**
   * @return the method name
   */
  @Nonnull
  public String getName() {
    return getData().getName();
  }

  /**
   * @return {@linkplain Class#getName() full name} of the method return type
   */
  @Nonnull
  public String getReturnType() {
    return getData().getReturnType();
  }

  /**
   * @return {@linkplain Class#getName() full names} of the method parameter types
   */
  @Nonnull
  @SuppressWarnings("unchecked")
  public List<String> getParameterTypes() {
    return (List<String>) getData().getParameterTypes();
  }

  /**
   * @return {@linkplain Class#getName() full name} of the declaring class of the method
   */
  @Nonnull
  public String getDeclaringClass() {
    return getData().getDefiningClass();
  }

  /**
   * @return a boolean indicating whether the method is a constructor
   */
  public boolean isConstructor() {
    return getName().equals("<init>");
  }

  /**
   * Checks if the reference contains the specified value.
   * <p>
   * More precisely, it returns {@code true} if at least one
   * {@link MethodRefData constant} value of the reference is {@code equal} to the specified value.
   */
  @Override
  public boolean contains(@Nonnull String value) {
    return getName().equals(value) || details && (
            getDeclaringClass().equals(value) || getReturnType().equals(value) || getParameterTypes().contains(value)
    );
  }

  /**
   * Returns the signature of the method.
   * <p>
   * Format: class.<b>method</b>(param1,param2,...paramN):returnType <br>
   * Example: java.lang.Byte.<b><u>{@linkplain Byte#parseByte parseByte}</u></b>(java.lang.String,int):byte
   * <p>
   * <b>Note:</b> A new signature string is generated with each method invocation.
   * @return method signature
   */
  @Nonnull
  @Override
  public String getSignature() {
    return !details ? DexUtils.getMethodSignature(getName()) :
            DexUtils.getMethodSignature(getDeclaringClass(), getName(), getParameterTypes(), getReturnType());
  }

  /**
   * @return a {@link MethodData} object representing the method
   */
  @Nonnull
  public MethodData toMethodData() {
    return MethodData.of(this);
  }

  @Override
  public int hashCode() {
    return getData().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return (this == obj) || (obj instanceof MethodRefData) && (
            this.getData().equals(((MethodRefData) obj).getData())
    );
  }

  /**
   * Equivalent to {@link #getSignature()}
   */
  @Override
  public String toString() {
    return getSignature();
  }
}
