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
import io.github.neonorbit.dexplore.reference.FieldRefData;
import io.github.neonorbit.dexplore.util.DexUtils;
import io.github.neonorbit.dexplore.util.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a dex field.
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public final class FieldData extends BaseItemData implements DexItemData, Comparable<FieldData> {
  private static final String DLM = ":";
  private static final String HEADER = "f";

  /** The declaring class of the field. */
  @Nonnull public final String clazz;
  /** The name of the field. */
  @Nonnull public final String field;
  /** The type of the field. */
  @Nonnull public final String type;

  private Object value = null;
  private ReferencePool referencePool;

  FieldData(@Nonnull String clazz,
            @Nonnull String field,
            @Nonnull String type) {
    this.clazz = clazz;
    this.field = field;
    this.type = type;
  }

  public static FieldData of(@Nonnull String clazz,
                             @Nonnull String field,
                             @Nonnull String type) {
    return new FieldData(
            Objects.requireNonNull(clazz),
            Objects.requireNonNull(field),
            Objects.requireNonNull(type)
    );
  }

  public static FieldData of(@Nonnull FieldRefData field) {
    return of(field.getDeclaringClass(), field.getName(), field.getType());
  }

  public static FieldData of(@Nonnull Field field) {
    return of(field.getDeclaringClass().getName(), field.getName(), field.getType().getName());
  }

  void setValue(Object value) {
    this.value = value;
  }

  void setReferencePool(ReferencePool referencePool) {
    this.referencePool = referencePool;
  }

  @Nullable
  public Field loadField(@Nonnull ClassLoader classLoader) {
    try {
      return Utils.loadClass(classLoader, clazz).getDeclaredField(field);
    } catch (ClassNotFoundException | NoSuchFieldException e) {
      return null;
    }
  }

  /**
   * If the field is a compile-time constant,
   * returns its value, otherwise returns null.<br><br>
   * Note: Make sure to check the field type and cast it accordingly.<br>
   * Examples:
   *   <pre>  if (field.getValue() instanceof Long) {...}</pre>
   *   <pre>  if (field.type.equals(long.class.getName())) {...}</pre>
   * @return the initial value of the field
   */
  @Nullable
  public Object getInitialValue() {
    return value;
  }

  /**
   * Returns the declaring class of the field.
   * @return {@linkplain Class#getName() full name} of the declaring class
   */
  @Nonnull
  @Override
  public String getClazz() {
    return clazz;
  }

  /**
   * Returns the {@code ReferencePool} of the field.
   * <p>Note: If the field is a compile-time constant and its type is String,
   * the returned pool will have a single item containing the String.
   * Otherwise an empty pool is returned.</p>
   *
   * @return the {@code ReferencePool} of the field
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
   * Signature: className.<b>fieldName</b>:fieldType
   * <p>Example: java.lang.Byte.<b>SIZE</b>:int</p>
   *
   * @return field signature
   */
  @Nonnull
  @Override
  public String getSignature() {
    return DexUtils.getFieldSignature(clazz, field, type);
  }

  /**
   * Serializes the object into a string.
   * <p>
   *   Includes: {@link #clazz}, {@link #field}, {@link #type}
   * </p>
   *
   * @return the serialized string
   */
  @Nonnull
  @Override
  public String serialize() {
    return HEADER + DLM + clazz + DLM + field + DLM + type;
  }

  /**
   * De-serializes the given string.
   *
   * @param serialized the string to be de-serialized
   * @return the de-serialized object
   * @throws IllegalArgumentException if the given string is not serializable
   */
  @Nonnull
  public static FieldData deserialize(@Nonnull String serialized) {
    final String[] parts = serialized.split(DLM);
    if (parts.length == 4 && parts[0].equals(HEADER) &&
        Arrays.stream(parts).noneMatch(String::isEmpty)) {
      return new FieldData(parts[1], parts[2], parts[3]);
    }
    throw new IllegalArgumentException("Invalid format: " + serialized);
  }

  @Override
  public int compareTo(@Nonnull FieldData o) {
    int compare;
    if (this == o) return 0;
    compare = this.clazz.compareTo(o.clazz);
    if (compare != 0) return compare;
    compare = this.field.compareTo(o.field);
    if (compare != 0) return compare;
    compare = this.type.compareTo(o.type);
    return compare;
  }

  @Override
  public int hashCode() {
    return Objects.hash(clazz, field, type);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj instanceof FieldData) {
      FieldData another = (FieldData) obj;
      return this.clazz.equals(another.clazz) &&
             this.field.equals(another.field) &&
             this.type.equals(another.type);
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
