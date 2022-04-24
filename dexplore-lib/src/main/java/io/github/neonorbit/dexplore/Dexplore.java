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

import io.github.neonorbit.dexplore.filter.ClassFilter;
import io.github.neonorbit.dexplore.filter.DexFilter;
import io.github.neonorbit.dexplore.filter.MethodFilter;
import io.github.neonorbit.dexplore.result.ClassData;
import io.github.neonorbit.dexplore.result.MethodData;
import io.github.neonorbit.dexplore.util.Operator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface Dexplore {
  @Nullable
  ClassData findClass(@Nonnull DexFilter dexFilter,
                      @Nonnull ClassFilter classFilter);

  @Nonnull
  List<ClassData> findClasses(@Nonnull DexFilter dexFilter,
                              @Nonnull ClassFilter classFilter, int limit);

  @Nullable
  MethodData findMethod(@Nonnull DexFilter dexFilter,
                        @Nonnull ClassFilter classFilter,
                        @Nonnull MethodFilter methodFilter);

  @Nonnull
  List<MethodData> findMethods(@Nonnull DexFilter dexFilter,
                               @Nonnull ClassFilter classFilter,
                               @Nonnull MethodFilter methodFilter, int limit);

  void onClassResults(@Nonnull DexFilter dexFilter,
                      @Nonnull ClassFilter classFilter,
                      @Nonnull Operator<ClassData> operator);

  void onMethodResults(@Nonnull DexFilter dexFilter,
                       @Nonnull ClassFilter classFilter,
                       @Nonnull MethodFilter methodFilter,
                       @Nonnull Operator<MethodData> operator);
}
