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
import io.github.neonorbit.dexplore.reference.TypeRefData;
import io.github.neonorbit.dexplore.util.ShallowList;
import io.github.neonorbit.dexplore.util.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Represents a dex class.
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public final class ClassData implements DexItemData, Comparable<ClassData> {
  private static final String DLM = ":";
  private static final String HEADER = "c";

  /** The {@linkplain Class#getName() full name} of the class. */
  @Nonnull public final String clazz;

  private List<FieldData> fields;
  private Map<String, MethodData> methods;
  private ReferencePool referencePool;

  ClassData(@Nonnull String clazz) {
    this.clazz = clazz;
  }

  public static ClassData of(@Nonnull String clazz) {
    return new ClassData(Objects.requireNonNull(clazz));
  }

  public static ClassData of(@Nonnull TypeRefData type) {
    return of(Objects.requireNonNull(type).getType());
  }

  public static ClassData of(@Nonnull Class<?> clazz) {
    return of(Objects.requireNonNull(clazz).getName());
  }

  void setFields(List<FieldData> fields) {
    this.fields = fields;
  }

  void setMethods(Map<String, MethodData> methods) {
    this.methods = methods;
  }

  MethodData getMethodBySignature(String signature) {
    return Objects.requireNonNull(methods).get(signature);
  }

  @Nullable
  public Class<?> loadClass(@Nonnull ClassLoader classLoader) {
    try {
      return Utils.loadClass(classLoader, clazz);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  /**
   * Returns a list of {@code FieldData} objects
   * representing all the fields of the class.
   * <p>The returned list is unmodifiable.</p>
   *
   * @return a list containing the fields of the class
   */
  public List<FieldData> getFields() {
    if (fields == null) {
      fields = Collections.emptyList();
    }
    return fields;
  }

  /**
   * @param name the name of the field
   * @return the {@code FieldData} object of the specified field
   */
  @Nullable
  public FieldData getField(@Nonnull String name) {
    return getFields().stream().filter(f -> f.field.equals(name)).findFirst().orElse(null);
  }

  /**
   * @param name the name of the method
   * @param params parameters or null if no params
   * @param returnType the return type of the method
   * @return the {@code MethodData} object of the specified method or null
   */
  @Nullable
  public MethodData getMethod(@Nonnull String name,
                              @Nullable List<String> params,
                              @Nonnull String returnType) {
    return getMethods().stream().filter(m -> m.method.equals(name) &&
            m.returnType.equals(returnType) && Utils.isEquals(params, m.params)
    ).findFirst().orElse(null);
  }

  /**
   * Returns a list containing {@code MethodData} objects
   * representing all the declared methods of the class.
   * <p>The returned list is unmodifiable.</p>
   *
   * @return a list containing the declared methods of the class
   */
  @Nonnull
  public List<MethodData> getMethods() {
    if (methods == null) {
      methods = Collections.emptyMap();
    }
    return ShallowList.of(methods.values());
  }

  /**
   * @param params parameter types if any
   * @return the {@code MethodData} object of the specified constructor or null
   */
  @Nullable
  public MethodData getConstructor(@Nonnull String... params) {
    return getMethods().stream().filter(m ->
            m.isConstructor() && Arrays.equals(params, m.params)
    ).findFirst().orElse(null);
  }

  /**
   * Returns a list of {@code MethodData} objects
   * representing all the declared constructors of the class.
   * <p>Note: Each time the method is invoked, a new list is created.</p>
   *
   * @return a list containing the declared constructors of the class
   */
  @Nonnull
  public List<MethodData> getConstructors() {
    return methods.values().stream().filter(MethodData::isConstructor).collect(toList());
  }

  /**
   * The {@linkplain Class#getName() full name} of the class.
   * @return full name of the class
   */
  @Nonnull
  @Override
  public String getClazz() {
    return clazz;
  }

  /**
   * Returns the {@code ReferencePool} of the class.
   * <p>It contains all the {@linkplain io.github.neonorbit.dexplore.reference references}
   * present in the class.</p>
   *
   * @return the {@code ReferencePool} of the class
   */
  @Nonnull
  @Override
  public ReferencePool getReferencePool() {
    if (referencePool == null) {
      referencePool = ReferencePool.merge(Stream
              .concat(fields.stream(), methods.values().stream())
              .map(DexItemData::getReferencePool).filter(r -> !r.isEmpty()).collect(toList())
      );
    }
    return referencePool;
  }

  /**
   * Signature: fullClassName
   * <p>Example: java.lang.Byte
   *
   * @return class signature
   */
  @Nonnull
  @Override
  public String getSignature() {
    return clazz;
  }

  /**
   * Serializes the object into a string.
   * <p> Includes: {@link #clazz} </p>
   *
   * @return the serialized string
   */
  @Nonnull
  @Override
  public String serialize() {
    return HEADER + DLM + this.clazz;
  }

  /**
   * De-serializes the given string.
   *
   * @param serialized the string to be de-serialized
   * @return the de-serialized object
   * @throws IllegalArgumentException if the given string is not serializable
   */
  @Nonnull
  public static ClassData deserialize(@Nonnull String serialized) {
    final String[] parts = serialized.split(DLM);
    if (parts.length == 2 && parts[0].equals(HEADER)) {
      final String clazz = parts[1];
      if (!clazz.isEmpty()) {
        return new ClassData(clazz);
      }
    }
    throw new IllegalArgumentException("Invalid format: " + serialized);
  }

  @Override
  public int compareTo(@Nonnull ClassData o) {
    return this.clazz.compareTo(o.clazz);
  }

  @Override
  public int hashCode() {
    return clazz.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return (this == obj) || (obj instanceof ClassData) && (
            this.clazz.equals(((ClassData) obj).clazz)
    );
  }

  /**
   * Equivalent to {@link #getSignature()}
   */
  @Override
  public String toString() {
    return clazz;
  }
}
