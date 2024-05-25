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

import io.github.neonorbit.dexplore.reference.DexRefData;
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
 * Represents a container for storing the references found within a specific dex file, class, or method.
 * <p>
 * See the detailed explanation <b>{@linkplain io.github.neonorbit.dexplore.reference here}</b>.
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
    if (pools.isEmpty() || pools.stream().allMatch(ReferencePool::isEmpty)) return EMPTY_POOL;
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

  /**
   * Returns the string section of the pool.
   * @return a list containing the {@linkplain StringRefData string} references of the pool
   */
  @Nonnull
  public List<StringRefData> getStringSection() {
    return strings;
  }

  /**
   * Returns the type section of the pool.
   * @return a list containing the {@linkplain TypeRefData type} references of the pool
   */
  @Nonnull
  public List<TypeRefData> getTypeSection() {
    return types;
  }

  /**
   * Returns the field section of the pool.
   * @return a list containing the {@linkplain FieldRefData field} references of the pool
   */
  @Nonnull
  public List<FieldRefData> getFieldSection() {
    return fields;
  }

  /**
   * Returns the method section of the pool.
   * @return a list containing the {@linkplain MethodRefData method} references of the pool
   */
  @Nonnull
  public List<MethodRefData> getMethodSection() {
    return methods;
  }

  /**
   * Returns the constructor section of the pool.
   * <p>
   *   <b>Note:</b> A new list is created with each method invocation.
   * </p>
   * @return a list containing the constructor references of the pool
   */
  @Nonnull
  public List<MethodRefData> getConstructorSection() {
    return methods.stream().filter(MethodRefData::isConstructor).collect(toList());
  }

  /**
   * Returns {@code true} if the pool contains the specified {@code value}.
   * <p>
   *   More precisely, it returns {@code true} if and only if at least one reference within the pool
   *   satisfies the condition {@link DexRefData#contains(String) DexRefData.contains(value)}.
   * </p>
   * @param value the value to compare against
   * @return {@code true} if the pool contains the specified value
   * @see #containsSignature(String)
   */
  public boolean contains(@Nonnull String value) {
    return stringsContain(value) || typesContain(value) || fieldsContain(value) || methodsContain(value);
  }

  /**
   * Returns {@code true} if the pool contains the specified signature.
   * <p>
   *   More precisely, it returns {@code true} if and only if at least one reference
   *   {@link DexRefData#getSignature() signature} within the pool matches the specified signature.
   * </p>
   * @param signature the signature to compare against
   * @return {@code true} if the pool contains the specified signature
   * @see #contains(String)
   */
  public boolean containsSignature(@Nonnull String signature) {
    return stringsContain(signature) || typesContain(signature) ||
            fieldSignaturesContain(signature) || methodSignaturesContain(signature);
  }

  /**
   * Returns {@code true} if the pool contains the specified value in its {@code string} references.
   * <p>
   *   This is equivalent to {@link #contains(String) contains(value)},
   *   except that it only checks within the {@code string} references of the pool.
   * </p>
   *
   * @param value the value to compare against
   * @return {@code true} if the specified value is found within the string references of the pool
   * @see #getStringSection()
   */
  public boolean stringsContain(@Nonnull String value) {
    return strings.stream().anyMatch(s -> s.contains(value));
  }

  /**
   * Returns {@code true} if the pool contains the specified value in its {@code type} references.
   * <p>
   *   This is equivalent to {@link #contains(String) contains(value)},
   *   except that it only checks within the {@code type} references of the pool.
   * </p>
   * @param value the value to compare against
   * @return {@code true} if the specified value is found within the type references of the pool
   * @see #getTypeSection()
   */
  public boolean typesContain(@Nonnull String value) {
    return types.stream().anyMatch(t -> t.contains(value));
  }

  /**
   * Returns {@code true} if the pool contains the specified value in its {@code field} references.
   * <p>
   *   This is equivalent to {@link #contains(String) contains(value)},
   *   except that it only checks within the {@code field} references of the pool.
   * </p>
   * @param value the value to compare against
   * @return {@code true} if the specified value is found within the field references of the pool
   * @see #getFieldSection()
   */
  public boolean fieldsContain(@Nonnull String value) {
    return fields.stream().anyMatch(f -> f.contains(value));
  }

  /**
   * Returns {@code true} if the pool contains the specified value in its {@code method} references.
   * <p>
   *   This is equivalent to {@link #contains(String) contains(value)},
   *   except that it only checks within the {@code method} references of the pool.
   * </p>
   * @param value the value to compare against
   * @return {@code true} if the specified value is found within the method references of the pool
   * @see #getMethodSection()
   */
  public boolean methodsContain(@Nonnull String value) {
    return methods.stream().anyMatch(m -> m.contains(value));
  }

  /**
   * Returns {@code true} if the pool contains the specified signature in its {@code field} references.
   * <p>
   * This is equivalent to {@link #containsSignature(String) containsSignature(sig)},
   * except that it only checks within the {@code field} references of the pool.
   * <p>
   * See Also: {@linkplain FieldRefData#getSignature() Field Signature}.
   *
   * @param signature the signature to compare against
   * @return {@code true} if the specified signature is found within the field references of the pool
   */
  public boolean fieldSignaturesContain(@Nonnull String signature) {
    return fields.stream().anyMatch(f -> f.getSignature().equals(signature));
  }

  /**
   * Returns {@code true} if the pool contains the specified signature in its {@code method} references.
   * <p>
   * This is equivalent to {@link #containsSignature(String) containsSignature(sig)},
   * except that it only checks within the {@code method} references of the pool.
   * <p>
   * See Also: {@linkplain MethodRefData#getSignature() Method Signature}.
   *
   * @param signature the signature to compare against
   * @return {@code true} if the specified signature is found within the method references of the pool
   */
  public boolean methodSignaturesContain(@Nonnull String signature) {
    return methods.stream().anyMatch(m -> m.getSignature().equals(signature));
  }

  /**
   * Returns a string representing all the reference signatures of the pool, each separated by a newline.
   * <p><b>Note:</b> A new string is generated with each method invocation.</p>
   *
   * @return a string representing all the reference signatures of the pool
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
