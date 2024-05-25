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
import io.github.neonorbit.dexplore.filter.ClassFilter;
import io.github.neonorbit.dexplore.filter.MethodFilter;

import javax.annotation.Nonnull;
import java.lang.reflect.Modifier;

/**
 * Represents a dex item (class, field or method).
 *
 * @see ClassData
 * @see FieldData
 * @see MethodData
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public interface DexItemData {
  /**
   * Returns the access {@link Modifier modifiers} of the item.
   * <p>
   *   <b>Note:</b> Modifiers are only available if the item was obtained from a search result.
   * </p>
   * @return the access {@link Modifier modifiers} of the item
   * or {@link Integer#MIN_VALUE MIN_VALUE} if not available.
   */
  int getModifiers();

  /**
   * Returns whether the item is synthetic.
   * <p>
   * <b>Note:</b> If the item was NOT obtained from a search result,
   * it will return false even if it is synthetic. See {@link #getModifiers()}.
   * <p>
   * <b>Note:</b> By default, synthetic items are not included in the search result.
   * <br>See the following methods:
   * <ul>
   *   <li>{@link ClassFilter.Builder#enableSyntheticClasses(boolean) enableSyntheticClasses()}</li>
   *   <li>{@link ClassFilter.Builder#enableSyntheticMembers(boolean) enableSyntheticMembers()}</li>
   *   <li>{@link MethodFilter.Builder#enableSyntheticMethods(boolean) enableSyntheticMethods()}</li>
   * </ul>
   *
   * @return a boolean indicating whether the item is synthetic
   */
  boolean isSynthetic();

  /**
   * Returns the full class name associated with the item.
   *
   * @return full class name
   * @see ClassData#getClazz()
   * @see FieldData#getClazz()
   * @see MethodData#getClazz()
   */
  @Nonnull
  String getClazz();

  /**
   * Returns the {@linkplain ReferencePool} of the item.
   *
   * @return the reference pool of the item
   * @see ClassData#getReferencePool()
   * @see FieldData#getReferencePool()
   * @see MethodData#getReferencePool()
   */
  @Nonnull
  ReferencePool getReferencePool();

  /**
   * Returns the signature of the dex item.
   * <p>
   * Signature Formats:
   * <ul>
   *   <li>
   *     {@linkplain ClassData}: <br>
   *     Format: FullClassName <br>
   *     Example: java.lang.Byte <br>
   *     .
   *   </li>
   *   <li>
   *     {@linkplain FieldData}: <br>
   *     Format: class.<b>fieldName</b>:fieldType <br>
   *     Example: java.lang.Byte.<b><u>{@linkplain Byte#BYTES BYTES}</u></b>:int <br>
   *     .
   *   </li>
   *   <li>
   *     {@linkplain MethodData}: <br>
   *     Format: class.<b>method</b>(param1,param2,...paramN):returnType <br>
   *     Example: java.lang.Byte.<b><u>{@linkplain Byte#parseByte parseByte}</u></b>(java.lang.String,int):byte
   *   </li>
   * </ul>
   *
   * @return dex item signature
   */
  @Nonnull
  String getSignature();

  /**
   * Serializes the object into a string.
   * <p>
   * <b>Note:</b> The serialized objects can be deserialized using the following methods:
   * <ul>
   *   <li>{@link ClassData#deserialize(String)}</li>
   *   <li>{@link FieldData#deserialize(String)}</li>
   *   <li>{@link MethodData#deserialize(String)}</li>
   * </ul>
   *
   * @return the serialized string
   */
  @Nonnull
  String serialize();
}
