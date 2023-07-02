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
import io.github.neonorbit.dexplore.util.DexUtils;
import io.github.neonorbit.dexplore.util.Utils;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.List;

/**
 * This class represents a {@linkplain io.github.neonorbit.dexplore.reference reference} to a method.
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
   * Equivalent to {@link Method#getName()}
   * @return method name
   */
  @Nonnull
  public String getName() {
    return getData().getName();
  }

  /**
   * Equivalent to {@link Method#getReturnType()}
   * @return return type
   */
  @Nonnull
  public String getReturnType() {
    return getData().getReturnType();
  }

  /**
   * Equivalent to {@link Method#getParameterTypes()}
   * @return parameter types
   */
  @Nonnull
  @SuppressWarnings("unchecked")
  public List<String> getParameterTypes() {
    return (List<String>) getData().getParameterTypes();
  }

  /**
   * Equivalent to {@link Method#getDeclaringClass()}
   * @return declaring class
   */
  @Nonnull
  public String getDeclaringClass() {
    return getData().getDefiningClass();
  }

  /**
   * @return whether the method is a constructor
   */
  public boolean isConstructor() {
    return getName().equals("<init>");
  }

  /**
   * Checks whether any items of this {@code MethodReference} match the specified string
   */
  @Override
  public boolean contains(@Nonnull String value) {
    return getName().equals(value) || details && (
            getDeclaringClass().equals(value) || getReturnType().equals(value) || getParameterTypes().contains(value)
    );
  }

  /**
   * Signature: class.<b>method</b>(param1,param2,...paramN):returnType
   * <p> Example: com.util.Time.<b>setNow</b>(int,java.lang.String,int):int </p>
   * <p>Each time the method is invoked, a new string is generated.</p>
   *
   * @return method signature
   */
  public String getSignature() {
    return !details ? DexUtils.getMethodSignature(getName()) :
            DexUtils.getMethodSignature(getDeclaringClass(), getName(), getParameterTypes(), getReturnType());
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
