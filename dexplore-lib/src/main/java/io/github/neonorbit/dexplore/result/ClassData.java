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
 * <p>
 * Properties:
 * <ul>
 *   <li>{@link #clazz} - full class name.</li>
 * </ul>
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public final class ClassData extends BaseItemData implements DexItemData, Comparable<ClassData> {
  private static final String DLM = ":";
  private static final String HEADER = "c";

  /** The full name of the class. */
  @Nonnull public final String clazz;

  private List<FieldData> fields;
  private Map<String, MethodData> methods;
  private ReferencePool referencePool;

  ClassData(@Nonnull String clazz) {
    this.clazz = clazz;
  }

  @Nonnull
  public static ClassData of(@Nonnull String clazz) {
    return new ClassData(Objects.requireNonNull(clazz));
  }

  @Nonnull
  public static ClassData of(@Nonnull TypeRefData type) {
    return of(Objects.requireNonNull(type).getType());
  }

  @Nonnull
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

  /**
   * Loads the {@code Class} object associated with the dex class.
   * @param classLoader the class loader to use
   * @return the {@code Class} object representing the class, or null if not found
   */
  @Nullable
  public Class<?> loadClass(@Nonnull ClassLoader classLoader) {
    try {
      return Utils.loadClass(classLoader, clazz);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  /**
   * Returns the specified field of the dex class.
   * @param name the name of the field
   * @return the {@code FieldData} object of the specified field or null
   */
  @Nullable
  public FieldData getField(@Nonnull String name) {
    return getFields().stream().filter(f -> f.field.equals(name)).findFirst().orElse(null);
  }

  /**
   * Returns a list containing all the declared fields of the dex class.
   * <p>The returned list is unmodifiable.</p>
   * @return a list of {@code FieldData} objects representing all the declared fields of the dex class
   */
  @Nonnull
  public List<FieldData> getFields() {
    if (fields == null) {
      fields = Collections.emptyList();
    }
    return fields;
  }

  /**
   * Returns the specified method of the dex class.
   * @param name the name of the method
   * @param params params or empty list if none
   * @param returnType the return type of the method
   * @return the {@code MethodData} object of the specified method or null
   */
  @Nullable
  public MethodData getMethod(@Nonnull String name,
                              @Nonnull List<String> params,
                              @Nonnull String returnType) {
    return getMethods().stream().filter(m -> m.method.equals(name) &&
            m.returnType.equals(returnType) && Utils.isEquals(params, m.params)
    ).findFirst().orElse(null);
  }

  /**
   * Returns a list containing all the declared methods of the dex class.
   * <p>The returned list is unmodifiable.</p>
   * @return a list of {@code MethodData} objects representing all the declared methods of the dex class
   */
  @Nonnull
  public List<MethodData> getMethods() {
    if (methods == null) {
      methods = Collections.emptyMap();
    }
    return ShallowList.of(methods.values());
  }

  /**
   * Returns the constructor with specified parameters.
   * <p>Call without any parameters to get the default constructor.</p>
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
   * Returns a list containing all the declared constructors of the dex class.
   * <p><b>Note:</b> A new list is created with each method invocation.</p>
   * @return a list of {@code MethodData} objects representing all the declared constructors of the dex class
   */
  @Nonnull
  public List<MethodData> getConstructors() {
    return methods.values().stream().filter(MethodData::isConstructor).collect(toList());
  }

  /**
   * Returns the full name of the class.
   * @return full name of the class
   */
  @Nonnull
  @Override
  public String getClazz() {
    return clazz;
  }

  /**
   * Returns the {@code ReferencePool} associated with the dex class.
   * <p>
   *   The returned pool contains all the {@linkplain io.github.neonorbit.dexplore.reference references}
   *   found within the class.
   * </p>
   * @return the {@code ReferencePool} of the dex class
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
   * Returns the signature of the class.
   * <p>
   *   Format: FullClassName <br>
   *   Example: java.lang.Byte
   * </p>
   * @return class signature
   */
  @Nonnull
  @Override
  public String getSignature() {
    return clazz;
  }

  /**
   * Serializes the object into a string.
   * <p>
   *   Includes: {@link #clazz}
   * <p>
   * <b>Note:</b> The serialized object can be deserialized
   * using the {@link #deserialize(String) deserialize()} method.
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
   * @param serialized the string to be deserialized
   * @return the deserialized object
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
    return getSignature();
  }
}
