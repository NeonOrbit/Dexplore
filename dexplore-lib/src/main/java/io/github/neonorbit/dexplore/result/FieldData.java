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
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Objects;

/**
 * Contains information about a field found in a dex file.
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public final class FieldData implements DexItemData, Comparable<FieldData> {
  /** The declaring class of the field. */
  @Nonnull public final String clazz;
  /** The name of the field. */
  @Nonnull public final String field;
  /** The type of the field. */
  @Nonnull public final String type;

  FieldData(@Nonnull String clazz,
            @Nonnull String field,
            @Nonnull String type) {
    this.clazz = clazz;
    this.field = field;
    this.type = type;
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
   * The declaring class of the field.
   * @return declaring class {@linkplain Class#getName() name}
   */
  @Nonnull
  @Override
  public String getClazz() {
    return clazz;
  }

  /**
   * @return an empty {@code ReferencePool}
   */
  @Nonnull
  @Override
  public ReferencePool getReferencePool() {
    return ReferencePool.emptyPool();
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
    return "f:" + clazz + ':' + field + ':' + type;
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
    final String[] parts = serialized.split(":");
    if (parts.length == 4 && parts[0].equals("f") &&
        Arrays.stream(parts).noneMatch(String::isEmpty)) {
      return new FieldData(parts[1], parts[2], parts[3]);
    }
    throw new IllegalArgumentException();
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
