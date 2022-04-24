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
import io.github.neonorbit.dexplore.result.Results;
import io.github.neonorbit.dexplore.util.Operator;
import io.github.neonorbit.dexplore.util.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

final class DexploreImpl implements Dexplore {
  private final DexOperation dexOperation;

  DexploreImpl(String path) {
    this(path, DexOptions.getDefault());
  }

  DexploreImpl(String path, DexOptions options) {
    this.dexOperation = new DexOperation(path, options);
  }

  @Nullable
  public ClassData findClass(@Nonnull DexFilter dexFilter,
                             @Nonnull ClassFilter classFilter) {
    return Utils.findFirst(classQuery(dexFilter, classFilter, 1));
  }

  @Nonnull
  public List<ClassData> findClasses(@Nonnull DexFilter dexFilter,
                                     @Nonnull ClassFilter classFilter, int limit) {
    return classQuery(dexFilter, classFilter, limit);
  }

  @Nullable
  public MethodData findMethod(@Nonnull DexFilter dexFilter,
                               @Nonnull ClassFilter classFilter,
                               @Nonnull MethodFilter methodFilter) {
    return Utils.findFirst(methodQuery(dexFilter, classFilter, methodFilter, 1));
  }

  @Nonnull
  public List<MethodData> findMethods(@Nonnull DexFilter dexFilter,
                                      @Nonnull ClassFilter classFilter,
                                      @Nonnull MethodFilter methodFilter, int limit) {
    return methodQuery(dexFilter, classFilter, methodFilter, limit);
  }

  public void onClassResults(@Nonnull DexFilter dexFilter,
                             @Nonnull ClassFilter classFilter,
                             @Nonnull Operator<ClassData> operator) {
    dexOperation.onClasses(dexFilter, classFilter,
                           dexClass -> operator.operate(Results.ofClass(dexClass)));
  }

  public void onMethodResults(@Nonnull DexFilter dexFilter,
                              @Nonnull ClassFilter classFilter,
                              @Nonnull MethodFilter methodFilter,
                              @Nonnull Operator<MethodData> operator) {
    dexOperation.onMethods(dexFilter, classFilter, methodFilter,
                           dexMethod -> operator.operate(Results.ofMethod(dexMethod)));
  }

  private List<ClassData> classQuery(DexFilter dexFilter,
                                     ClassFilter classFilter, int limit) {
    List<ClassData> results = new ArrayList<>();
    dexOperation.onClasses(dexFilter, classFilter, dexClass -> {
      results.add(Results.ofClass(dexClass));
      return (limit > 0 && results.size() >= limit);
    });
    return results;
  }

  private List<MethodData> methodQuery(DexFilter dexFilter,
                                       ClassFilter classFilter,
                                       MethodFilter methodFilter, int limit) {
    List<MethodData> results = new ArrayList<>();
    AtomicReference<ClassData> shared = new AtomicReference<>();
    dexOperation.onMethods(dexFilter, classFilter, methodFilter, dexMethod -> {
      MethodData method = Results.ofMethod(dexMethod, shared.get());
      shared.set(method.getClassData());
      results.add(method);
      return (limit > 0 && results.size() >= limit);
    });
    return results;
  }
}
