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

package io.github.neonorbit.dexplore.iface;

/**
 * A callback interface to operate on key-based search results.
 * <p>
 *   The {@link #operate operate(key, item)} method is invoked for each result.
 *   Returning {@code true} for a given key immediately terminates the callback for that key
 *   and proceeds to the next key.
 * </p>
 *
 * @author NeonOrbit
 * @since 1.4.0
 */
public interface KOperator<T> {
  /**
   * A {@linkplain KOperator callback} method to operate on key-based search results.
   *
   * @param key the key associated with the item.
   * @param item the item to operate on.
   * @return {@code true} to terminate for the given key, {@code false} to continue.
   */
  boolean operate(String key, T item);
}
