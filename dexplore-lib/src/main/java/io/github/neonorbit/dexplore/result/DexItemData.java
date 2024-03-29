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

import javax.annotation.Nonnull;
import java.lang.reflect.Modifier;

public interface DexItemData {
  /**
   * <b>Note:</b> Modifiers are only available
   *  if the item was obtained from a search result.
   * @return the access {@link Modifier modifiers} of the item
   * or {@link Integer#MIN_VALUE} if not available.
   */
  int getModifiers();

  /**
   * <b>Note:</b> If the item was NOT obtained from a search result,
   * it will return false even if it is synthetic. See {@link #getModifiers()}.
   * @return a boolean indicating whether the item is synthetic
   */
  boolean isSynthetic();

  /**
   * @see ClassData#getClazz()
   * @see FieldData#getClazz()
   * @see MethodData#getClazz()
   * @return class name
   */
  @Nonnull
  String getClazz();

  /**
   * @see ClassData#getReferencePool()
   * @see FieldData#getReferencePool()
   * @see MethodData#getReferencePool()
   * @return the {@code ReferencePool} of the item
   */
  @Nonnull
  ReferencePool getReferencePool();

  /**
   * @see ClassData#getSignature()
   * @see FieldData#getSignature()
   * @see MethodData#getSignature()
   * @return item signature
   */
  @Nonnull
  String getSignature();

  /**
   * Serializes the object into a string.
   * @return the serialized string
   */
  @Nonnull
  String serialize();
}
