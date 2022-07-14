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

package io.github.neonorbit.dexplore.task;

import io.github.neonorbit.dexplore.Dexplore;
import io.github.neonorbit.dexplore.QueryBatch;
import io.github.neonorbit.dexplore.iface.Internal;
import io.github.neonorbit.dexplore.iface.KOperator;
import io.github.neonorbit.dexplore.result.DexItemData;

import javax.annotation.Nonnull;

@Internal
public final class QueryTaskFactory {
  private final Dexplore dexplore;
  private final KOperator<DexItemData> operator;

  public QueryTaskFactory(@Nonnull Dexplore dexplore,
                          @Nonnull KOperator<DexItemData> operator) {
    this.dexplore = dexplore;
    this.operator = operator;
  }

  @Nonnull
  public QueryTask newTask(@Nonnull QueryBatch.Query query) {
    return new QueryTask(query, dexplore, operator);
  }
}
