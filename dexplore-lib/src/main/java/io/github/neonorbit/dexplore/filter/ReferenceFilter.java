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
 * A filter for reference {@link ReferencePool pools}.
 * <p>
 * Instances of this interface may be passed to the
 * {@link BaseFilter.Builder#setReferenceFilter(ReferenceFilter)
 *        setReferenceFilter()}
 * method.
 * @see #accept(ReferencePool)
 *
 * @author NeonOrbit
 * @since 1.1.0
 */
public interface ReferenceFilter {
  /**
   * Tests whether the item containing the given reference pool should be matched.
   *
   * @param pool the reference pool to be tested
   * @return true if the item containing the pool should be matched
   */
  boolean accept(ReferencePool pool);

  default ReferenceFilter and(@Nonnull ReferenceFilter other) {
    return pool -> accept(pool) && other.accept(pool);
  }

  default ReferenceFilter or(@Nonnull ReferenceFilter other) {
    return pool -> accept(pool) || other.accept(pool);
  }

  static ReferenceFilter contains(@Nonnull String value) {
    return pool -> pool.contains(value);
  }

  static ReferenceFilter containsAny(@Nonnull String... value) {
    return pool -> Arrays.stream(value).anyMatch(pool::contains);
  }

  static ReferenceFilter containsAll(@Nonnull String... value) {
    return pool -> Arrays.stream(value).allMatch(pool::contains);
  }

  static ReferenceFilter stringsContain(@Nonnull String value) {
    return pool -> pool.stringsContain(value);
  }

  static ReferenceFilter fieldsContain(@Nonnull String value) {
    return pool -> pool.fieldsContain(value);
  }

  static ReferenceFilter methodsContain(@Nonnull String value) {
    return pool -> pool.methodsContain(value);
  }

  static ReferenceFilter typesContain(@Nonnull String value) {
    return pool -> pool.typesContain(value);
  }
}
