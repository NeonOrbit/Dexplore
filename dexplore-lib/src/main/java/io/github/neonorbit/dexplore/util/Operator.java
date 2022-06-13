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

package io.github.neonorbit.dexplore.util;

/**
 * A callback interface to operate on search results.
 * <p>
 *   {@link #operate operate(item)} will be called for each result that is produced,
 *   and should return a boolean to indicate whether the callback should be terminated.
 * </p>
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public interface Operator<T> {
  /**
   * @param item the item to operate on.
   * @return {@code true} to terminate, {@code false} to continue.
   */
  boolean operate(T item);
}
