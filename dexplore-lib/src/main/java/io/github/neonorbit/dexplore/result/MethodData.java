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

package io.github.neonorbit.dexplore.result;

import io.github.neonorbit.dexplore.ReferencePool;
import io.github.neonorbit.dexplore.util.DexUtils;
import io.github.neonorbit.dexplore.util.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Stream;

/**
 * Represents a dex method.
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public final class MethodData implements DexItemData, Comparable<MethodData> {
  private static final String HEADER = "m";

  /** The declaring class of the method. */
  @Nonnull public final String clazz;
  /** The name of the method. */
  @Nonnull public final String method;
  /** The parameters of the method. */
  @Nonnull public final String[] params;
  /** The return type of the method. */
  @Nonnull public final String returnType;

  private ClassData classData;
  private ReferencePool referencePool;

  MethodData(@Nonnull String clazz,
             @Nonnull String method,
             @Nonnull String[] params,
             @Nonnull String returnType) {
    this.clazz = clazz;
    this.method = method;
    this.params = params;
    this.returnType = returnType;
  }

  void setClassData(ClassData classData) {
    this.classData = classData;
  }

  void setReferencePool(ReferencePool referencePool) {
    this.referencePool = referencePool;
  }

  @Nullable
  public Method loadMethod(@Nonnull ClassLoader classLoader) {
    try {
      final Class<?>[] paramTypes = new Class[params.length];
      for (int i = 0; i < params.length; i++) {
        paramTypes[i] = Utils.loadClass(classLoader, params[i]);
      }
      return Utils.loadClass(classLoader, clazz).getDeclaredMethod(method, paramTypes);
    } catch (ClassNotFoundException | NoSuchMethodException e) {
      return null;
    }
  }

  /**
   * @return whether the method is a constructor
   */
  public boolean isConstructor() {
    return method.equals("<init>");
  }

  /**
   * @return the ClassData instance of the declaring class of the method
   */
  @Nonnull
  public ClassData getClassData() {
    if (classData == null) {
      classData = ClassData.deserialize(clazz);
    }
    return classData;
  }

  /**
   * The declaring class of the method.
   * @return declaring class {@linkplain Class#getName() name}
   */
  @Nonnull
  @Override
  public String getClazz() {
    return clazz;
  }

  /**
   * Returns the {@code ReferencePool} of the method.
   * <p>It contains all the {@linkplain io.github.neonorbit.dexplore.reference references}
   * present in the method.</p>
   *
   * @return the {@code ReferencePool} of the method
   */
  @Nonnull
  @Override
  public ReferencePool getReferencePool() {
    if (referencePool == null) {
      referencePool = ReferencePool.emptyPool();
    }
    return referencePool;
  }

  /**
   * Signature: class.<b>method</b>(param1,param2,...paramN):returnType
   * <p> Example: com.util.Time.<b>setNow</b>(int,java.lang.String,int):int </p>
   *
   * @return method signature
   */
  @Nonnull
  @Override
  public String getSignature() {
    return DexUtils.getMethodSignature(clazz, method, Arrays.asList(params), returnType);
  }

  /**
   * Serializes the object into a string.
   * <p>
   *   Includes: {@link #clazz}, {@link #method}, {@link #params}, {@link #returnType}
   * </p>
   *
   * @return the serialized string
   */
  @Nonnull
  @Override
  public String serialize() {
    StringJoiner joiner = new StringJoiner(":");
    joiner.add(HEADER).add(clazz).add(method);
    for (String param : params) {
      joiner.add(param);
    }
    joiner.add(returnType);
    return joiner.toString();
  }

  /**
   * De-serializes the given string.
   *
   * @param serialized the string to be de-serialized
   * @return the de-serialized object
   * @throws IllegalArgumentException if the given string is not serializable
   */
  @Nonnull
  public static MethodData deserialize(@Nonnull String serialized) {
    final String[] parts = serialized.split(":");
    if (parts.length >= 4 && parts[0].equals(HEADER)) {
      final String from = parts[1], name = parts[2], type = parts[parts.length - 1];
      final String[] params = Arrays.copyOfRange(parts, 3, parts.length - 1);
      if (Stream.of(name, from, type).noneMatch(String::isEmpty) &&
          Arrays.stream(params).noneMatch(String::isEmpty)) {
        return new MethodData(from, name, params, type);
      }
    }
    throw new IllegalArgumentException("Invalid format: " + serialized);
  }

  @Override
  public int compareTo(@Nonnull MethodData o) {
    int compare;
    if (this == o) return 0;
    compare = this.clazz.compareTo(o.clazz);
    if (compare != 0) return compare;
    compare = this.method.compareTo(o.method);
    if (compare != 0) return compare;
    compare = this.returnType.compareTo(o.returnType);
    if (compare != 0) return compare;
    return Utils.compare(this.params, o.params);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clazz, method, returnType, Arrays.hashCode(params));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj instanceof MethodData) {
      MethodData another = (MethodData) obj;
      return this.clazz.equals(another.clazz) &&
             this.method.equals(another.method) &&
             this.returnType.equals(another.returnType) &&
             Arrays.equals(this.params, another.params);
    }
    return false;
  }

  /**
   * Equivalent to {@link #getSignature()}
   */
  @Override
  public String toString() {
    return getSignature();
  }
}
