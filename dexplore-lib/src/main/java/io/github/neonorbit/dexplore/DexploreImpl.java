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

import io.github.neonorbit.dexplore.util.Utils;
import io.github.neonorbit.dexplore.filter.ClassFilter;
import io.github.neonorbit.dexplore.filter.DexFilter;
import io.github.neonorbit.dexplore.filter.MethodFilter;
import io.github.neonorbit.dexplore.result.ClassData;
import io.github.neonorbit.dexplore.result.MethodData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                                     @Nonnull ClassFilter classFilter, int maximum) {
    return classQuery(dexFilter, classFilter, maximum);
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
                                      @Nonnull MethodFilter methodFilter, int maximum) {
    return methodQuery(dexFilter, classFilter, methodFilter, maximum);
  }

  public void onClassSearchResults(@Nonnull DexFilter dexFilter,
                                   @Nonnull ClassFilter classFilter,
                                   @Nonnull Enumerator<ClassData> enumerator) {
    dexOperation.onClasses(dexFilter, classFilter,
                           dexClass -> enumerator.next(ClassData.from(dexClass)));
  }

  public void onMethodSearchResults(@Nonnull DexFilter dexFilter,
                                    @Nonnull ClassFilter classFilter,
                                    @Nonnull MethodFilter methodFilter,
                                    @Nonnull Enumerator<MethodData> enumerator) {
    dexOperation.onMethods(dexFilter, classFilter, methodFilter,
                           dexMethod -> enumerator.next(MethodData.from(dexMethod)));
  }

  private List<ClassData> classQuery(DexFilter dexFilter,
                                     ClassFilter classFilter, int maximum) {
    List<ClassData> results = new ArrayList<>();
    dexOperation.onClasses(dexFilter, classFilter, dexClass -> {
      results.add(ClassData.from(dexClass)); return (results.size() >= maximum);
    });
    return results;
  }

  private List<MethodData> methodQuery(DexFilter dexFilter,
                                       ClassFilter classFilter,
                                       MethodFilter methodFilter, int maximum) {
    Map<String, ClassData> shared = new HashMap<>();
    List<MethodData> results = new ArrayList<>();
    dexOperation.onMethods(dexFilter, classFilter, methodFilter, dexMethod -> {
      String clazz = dexMethod.classDef.getType();
      MethodData result = MethodData.from(dexMethod, shared.get(clazz));
      shared.put(clazz, result.getClassResult());
      results.add(result);
      return (results.size() >= maximum);
    });
    return results;
  }
}
