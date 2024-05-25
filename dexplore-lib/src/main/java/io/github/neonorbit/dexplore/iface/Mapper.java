/*
 * Copyright (C) 2023 NeonOrbit
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

import io.github.neonorbit.dexplore.reference.FieldRefData;
import io.github.neonorbit.dexplore.reference.MethodRefData;
import io.github.neonorbit.dexplore.reference.TypeRefData;
import io.github.neonorbit.dexplore.result.DexItemData;

import javax.annotation.Nonnull;

/**
 * A {@linkplain #map(DexItemData) mapper} function that transforms a given DexItemData into a different DexItemData.
 * <p> This is typically achieved by extracting references from the provided item's ReferencePool.
 * <br><br>
 * Examples:
 * <pre>{@code
 * item -> item.getReferencePool().getMethodSection().get(0).toMethodData();
 * item -> item.getFields().stream().filter(field -> field.type.equals("int")).findFirst().get();
 * }</pre>
 * <br>
 * Note: The references found in a ReferencePool can easily be transformed into different types of DexItemData:
 * <ul>
 *   <li>{@link TypeRefData#toClassData()}</li>
 *   <li>{@link FieldRefData#toFieldData()}</li>
 *   <li>{@link MethodRefData#toMethodData()}</li>
 * </ul>
 *
 * @param <T> type of the item to be transformed
 * @see #map(DexItemData) map(item)
 * @author NeonOrbit
 * @since 1.4.7
 */
public interface Mapper<T extends DexItemData> {
  /**
   * @param item the item to be transformed
   * @return the transformed item
   */
  DexItemData map(@Nonnull T item);
}
