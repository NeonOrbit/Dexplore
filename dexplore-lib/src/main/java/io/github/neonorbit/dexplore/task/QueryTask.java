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
import io.github.neonorbit.dexplore.QueryBatch.ClassQuery;
import io.github.neonorbit.dexplore.QueryBatch.MethodQuery;
import io.github.neonorbit.dexplore.QueryBatch.Query;
import io.github.neonorbit.dexplore.iface.Internal;
import io.github.neonorbit.dexplore.iface.KOperator;
import io.github.neonorbit.dexplore.result.DexItemData;

import javax.annotation.Nonnull;

@Internal
public final class QueryTask extends KeyedTask<Object> {
  private final Query query;
  private final Dexplore dexplore;
  private final KOperator<DexItemData> operator;

  QueryTask(@Nonnull Query query,
            @Nonnull Dexplore dexplore,
            @Nonnull KOperator<DexItemData> operator) {
    super(query.key);
    this.query = query;
    this.dexplore = dexplore;
    this.operator = operator;
  }

  @Override
  public Object run() {
    if (!(query instanceof MethodQuery)) {
      ClassQuery q = (ClassQuery) query;
      dexplore.onClassResult(q.dexFilter, q.classFilter, r -> operator.operate(q.key, r));
    } else {
      MethodQuery q = (MethodQuery) query;
      dexplore.onMethodResult(q.dexFilter, q.classFilter, q.methodFilter, r -> operator.operate(q.key, r));
    }
    return null;
  }
}
