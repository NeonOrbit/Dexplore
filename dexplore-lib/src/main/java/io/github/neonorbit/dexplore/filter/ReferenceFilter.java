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

package io.github.neonorbit.dexplore.filter;

import io.github.neonorbit.dexplore.ReferencePool;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * A filter to apply to the {@link ReferencePool} of each dex item.
 * <p>
 * Implementations of this interface may be passed to the {@link
 *    BaseFilter.Builder#setReferenceFilter(ReferenceFilter) setReferenceFilter()
 * } method.
 * @see #accept(ReferencePool)
 *
 * @author NeonOrbit
 * @since 1.1.0
 */
public interface ReferenceFilter {
  /**
   * Tests whether the item containing the provided reference pool should be matched.
   *
   * @param pool the reference pool to be tested
   * @return true if the item containing the pool should be matched
   */
  boolean accept(@Nonnull ReferencePool pool);

  /**
   * @return the logical negation of the filter
   */
  default ReferenceFilter negate() {
    return pool -> !accept(pool);
  }

  /**
   * @param other another filter
   * @return a new filter combining this and the other filter with a logical AND operator.
   */
  default ReferenceFilter and(@Nonnull ReferenceFilter other) {
    return pool -> accept(pool) && other.accept(pool);
  }

  /**
   * @param other another filter
   * @return a new filter combining this and the other filter with a logical OR operator.
   */
  default ReferenceFilter or(@Nonnull ReferenceFilter other) {
    return pool -> accept(pool) || other.accept(pool);
  }

  /**
   * Wrapper for {@link ReferencePool#contains(String)}
   * @param value the value to compare against
   * @return a reference filter
   */
  static ReferenceFilter contains(@Nonnull String value) {
    return pool -> pool.contains(value);
  }

  /**
   * Wrapper for {@link ReferencePool#containsSignature(String)}
   * @param signature the signature to compare against
   * @return a reference filter
   */
  static ReferenceFilter containsSignature(@Nonnull String signature) {
    return pool -> pool.containsSignature(signature);
  }

  /**
   * Returns a filter that tests if a reference pool
   * {@linkplain ReferencePool#contains(String) contains} any of the specified values.
   * @param values the values to compare against
   * @return a reference filter
   */
  static ReferenceFilter containsAny(@Nonnull String... values) {
    if (values.length == 0) throw new IllegalArgumentException();
    return pool -> Arrays.stream(values).anyMatch(pool::contains);
  }

  /**
   * Returns a filter that tests if a reference pool
   * {@linkplain ReferencePool#contains(String) contains} all the specified values.
   * @param values the values to compare against
   * @return a reference filter
   */
  static ReferenceFilter containsAll(@Nonnull String... values) {
    if (values.length == 0) throw new IllegalArgumentException();
    return pool -> Arrays.stream(values).allMatch(pool::contains);
  }

  /**
   * Wrapper for {@link ReferencePool#stringsContain(String)}
   * @param value the value to compare against
   * @return a reference filter
   */
  static ReferenceFilter stringsContain(@Nonnull String value) {
    return pool -> pool.stringsContain(value);
  }

  /**
   * Wrapper for {@link ReferencePool#typesContain(String)}
   * @param value the value to compare against
   * @return a reference filter
   */
  static ReferenceFilter typesContain(@Nonnull String value) {
    return pool -> pool.typesContain(value);
  }

  /**
   * Wrapper for {@link ReferencePool#fieldsContain(String)}
   * @param value the value to compare against
   * @return a reference filter
   */
  static ReferenceFilter fieldsContain(@Nonnull String value) {
    return pool -> pool.fieldsContain(value);
  }

  /**
   * Wrapper for {@link ReferencePool#methodsContain(String)}
   * @param value the value to compare against
   * @return a reference filter
   */
  static ReferenceFilter methodsContain(@Nonnull String value) {
    return pool -> pool.methodsContain(value);
  }
}
