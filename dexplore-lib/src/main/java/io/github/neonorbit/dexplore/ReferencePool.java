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

import io.github.neonorbit.dexplore.reference.FieldRefData;
import io.github.neonorbit.dexplore.reference.MethodRefData;
import io.github.neonorbit.dexplore.reference.StringRefData;
import io.github.neonorbit.dexplore.reference.TypeRefData;
import io.github.neonorbit.dexplore.util.MergedList;
import io.github.neonorbit.dexplore.util.Utils;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import static java.util.stream.Collectors.toList;

/**
 * An instance of this class holds the {@linkplain io.github.neonorbit.dexplore.reference references}
 * present in a dex file, class or method.
 *
 * @see StringRefData
 * @see TypeRefData
 * @see FieldRefData
 * @see MethodRefData
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public final class ReferencePool {
  private final static ReferencePool EMPTY_POOL;
  private final List<StringRefData> strings;
  private final List<TypeRefData> types;
  private final List<FieldRefData> fields;
  private final List<MethodRefData> methods;

  static {
    EMPTY_POOL = new ReferencePool(
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList(),
        Collections.emptyList()
    );
  }

  private ReferencePool(List<StringRefData> strings,
                        List<TypeRefData> types,
                        List<FieldRefData> fields,
                        List<MethodRefData> methods) {
    this.strings = strings;
    this.types = types;
    this.fields = fields;
    this.methods = methods;
  }

  static ReferencePool build(List<StringRefData> strings,
                             List<TypeRefData> types,
                             List<FieldRefData> fields,
                             List<MethodRefData> methods) {
    if (strings.isEmpty() && types.isEmpty() && fields.isEmpty() && methods.isEmpty()) {
      return EMPTY_POOL;
    } else {
      return new ReferencePool(
              Utils.optimizedList(strings),
              Utils.optimizedList(types),
              Utils.optimizedList(fields),
              Utils.optimizedList(methods)
      );
    }
  }

  public static ReferencePool merge(@Nonnull List<ReferencePool> pools) {
    if (pools.stream().allMatch(ReferencePool::isEmpty)) return EMPTY_POOL;
    return new ReferencePool(
            MergedList.merge(pools, ReferencePool::getStringSection),
            MergedList.merge(pools, ReferencePool::getTypeSection),
            MergedList.merge(pools, ReferencePool::getFieldSection),
            MergedList.merge(pools, ReferencePool::getMethodSection)
    );
  }

  public static ReferencePool emptyPool() {
    return EMPTY_POOL;
  }

  public boolean isEmpty() {
    return this == EMPTY_POOL;
  }

  @Nonnull
  public List<StringRefData> getStringSection() {
    return strings;
  }

  @Nonnull
  public List<FieldRefData> getFieldSection() {
    return fields;
  }

  @Nonnull
  public List<MethodRefData> getMethodSection() {
    return methods;
  }

  @Nonnull
  public List<TypeRefData> getTypeSection() {
    return types;
  }

  /**
   * Returns the constructor section of the pool.
   * <p>Each time the method is invoked, a new list is created.</p>
   * @return the constructor section of the pool
   */
  @Nonnull
  public List<MethodRefData> getConstructorSection() {
    return methods.stream().filter(MethodRefData::isConstructor).collect(toList());
  }

  /**
   * Checks whether any items of this {@code Pool} contain the specified string
   *
   * @param value The string to compare against
   * @return {@code true} if this {@code Pool} contains the given string
   */
  public boolean contains(@Nonnull String value) {
    return stringsContain(value) || typesContain(value) ||
            fieldsContain(value) || methodsContain(value);
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
   * Checks whether any {@code Field} items of this {@code Pool} contain
   * the specified {@link FieldRefData#getSignature() signature}.
   *
   * @param signature The signature to compare against
   * @return {@code true} if this {@code Pool} contains the given signature
   */
  public boolean fieldSignaturesContain(@Nonnull String signature) {
    return fields.stream().anyMatch(f -> f.getSignature().contains(signature));
  }

  /**
   * Checks whether any {@code Method} items of this {@code Pool} contain
   * the specified {@link MethodRefData#getSignature() signature}.
   *
   * @param signature The signature to compare against
   * @return {@code true} if this {@code Pool} contains the given signature
   */
  public boolean methodSignaturesContain(@Nonnull String signature) {
    return methods.stream().anyMatch(m -> m.getSignature().contains(signature));
  }

  /**
   * Returns a string containing all the reference signatures (separated by newline).
   * <p>Each time the method is invoked, a new string is generated.</p>
   * @return a string representing all the reference signatures
   */
  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner("\n");
    strings.forEach(s -> joiner.add(s.toString()));
    types.forEach(t -> joiner.add(t.toString()));
    fields.forEach(f -> joiner.add(f.toString()));
    methods.forEach(m -> joiner.add(m.toString()));
    return joiner.toString();
  }
}
