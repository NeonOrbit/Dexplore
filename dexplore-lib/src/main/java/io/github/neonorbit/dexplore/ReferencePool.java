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

package io.github.neonorbit.dexplore;

import io.github.neonorbit.dexplore.reference.FieldReferenceData;
import io.github.neonorbit.dexplore.reference.MethodReferenceData;
import io.github.neonorbit.dexplore.reference.StringReferenceData;
import io.github.neonorbit.dexplore.reference.TypeReferenceData;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

public final class ReferencePool {
  private String toString;
  private final static ReferencePool EMPTY_POOL;
  private final List<StringReferenceData> strings;
  private final List<TypeReferenceData> types;
  private final List<FieldReferenceData> fields;
  private final List<MethodReferenceData> methods;

  static {
    EMPTY_POOL = new ReferencePool(
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList()
    );
  }

  private ReferencePool(List<StringReferenceData> strings,
                        List<TypeReferenceData> types,
                        List<FieldReferenceData> fields,
                        List<MethodReferenceData> methods) {
    this.strings = Collections.unmodifiableList(strings);
    this.types = Collections.unmodifiableList(types);
    this.fields = Collections.unmodifiableList(fields);
    this.methods = Collections.unmodifiableList(methods);
  }

  static ReferencePool build(List<StringReferenceData> strings,
                             List<TypeReferenceData> types,
                             List<FieldReferenceData> fields,
                             List<MethodReferenceData> methods) {
    if (strings.isEmpty() && types.isEmpty() &&
        fields.isEmpty() && methods.isEmpty()) {
      return EMPTY_POOL;
    } else {
      return new ReferencePool(strings, types, fields, methods);
    }
  }

  public static ReferencePool emptyPool() {
    return EMPTY_POOL;
  }

  public boolean isEmpty() {
    return this == EMPTY_POOL;
  }

  @Nonnull
  public List<StringReferenceData> getStringSection() {
    return strings;
  }

  @Nonnull
  public List<FieldReferenceData> getFieldSection() {
    return fields;
  }

  @Nonnull
  public List<MethodReferenceData> getMethodSection() {
    return methods;
  }

  @Nonnull
  public List<TypeReferenceData> getTypeSection() {
    return types;
  }

  /**
   * Checks whether any items of this {@code Pool} contain the specified string
   *
   * @param value The string to compare against
   * @return {@code true} if this {@code Pool} contains the given string
   */
  public boolean contains(@Nonnull String value) {
    return stringsContain(value) ||
           fieldsContain(value)  ||
           methodsContain(value) ||
           typesContain(value);
  }

  /**
   * Checks whether any {@code String} items of this {@code Pool} contain the specified string
   *
   * @param value The string to compare against
   * @return {@code true} if this {@code Pool} contains the given string in its {@code String} section
   */
  public boolean stringsContain(@Nonnull String value) {
    return strings.stream().anyMatch(s -> s.contains(value));
  }

  /**
   * Checks whether any {@code Field} items of this {@code Pool} contain the specified string
   *
   * @param value The string to compare against
   * @return {@code true} if this {@code Pool} contains the given string in its {@code Field} section
   */
  public boolean fieldsContain(@Nonnull String value) {
    return fields.stream().anyMatch(f -> f.contains(value));
  }

  /**
   * Checks whether any {@code Method} items of this {@code Pool} contain the specified string
   *
   * @param value The string to compare against
   * @return {@code true} if this {@code Pool} contains the given string in its {@code Method} section
   */
  public boolean methodsContain(@Nonnull String value) {
    return methods.stream().anyMatch(m -> m.contains(value));
  }

  /**
   * Checks whether any {@code Type} items of this {@code Pool} contain the specified string
   *
   * @param value The string to compare against
   * @return {@code true} if this {@code Pool} contains the given string in its {@code Type} section
   */
  public boolean typesContain(@Nonnull String value) {
    return types.stream().anyMatch(t -> t.contains(value));
  }

  /**
   * Checks whether any {@code Field} items of this {@code Pool} contain the specified signature
   *
   * @param signature The signature to compare against
   * @return {@code true} if this {@code Pool} contains the given field signature
   *
   * @see FieldReferenceData#toString()
   */
  public boolean fieldSignaturesContain(@Nonnull String signature) {
    return fields.stream().anyMatch(f -> f.toString().contains(signature));
  }

  /**
   * Checks whether any {@code Method} items of this {@code Pool} contain the specified signature
   *
   * @param signature The signature to compare against
   * @return {@code true} if this {@code Pool} contains the given method signature
   *
   * @see MethodReferenceData#toString()
   */
  public boolean methodSignaturesContain(@Nonnull String signature) {
    return methods.stream().anyMatch(m -> m.toString().contains(signature));
  }

  @Override
  public String toString() {
    if (toString == null) {
      StringJoiner joiner = new StringJoiner("\n");
      strings.forEach(s -> joiner.add(s.toString()));
      types.forEach(t -> joiner.add(t.toString()));
      fields.forEach(f -> joiner.add(f.toString()));
      methods.forEach(m -> joiner.add(m.toString()));
      toString = joiner.toString();
    }
    return toString;
  }
}
