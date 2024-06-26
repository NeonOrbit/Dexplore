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
import io.github.neonorbit.dexplore.iface.Internal;
import io.github.neonorbit.dexplore.iface.KOperator;
import io.github.neonorbit.dexplore.iface.Operator;
import io.github.neonorbit.dexplore.result.ClassData;
import io.github.neonorbit.dexplore.result.DexItemData;
import io.github.neonorbit.dexplore.result.MethodData;
import io.github.neonorbit.dexplore.result.Results;
import io.github.neonorbit.dexplore.task.QueryTask;
import io.github.neonorbit.dexplore.task.QueryTaskFactory;
import io.github.neonorbit.dexplore.task.TaskHandler;
import io.github.neonorbit.dexplore.util.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Internal
final class DexploreImpl implements Dexplore {
  private final DexOperation dexOperation;
  private TaskHandler<Object> taskHandler;

  DexploreImpl(String path, DexOptions options) {
    this.dexOperation = new DexOperation(path, options);
  }

  DexploreImpl(byte[] buffer, DexOptions options) {
    this.dexOperation = new DexOperation(buffer, options);
  }

  private synchronized TaskHandler<Object> getTaskHandler(int size) {
    if (taskHandler == null || taskHandler.isDirty() || taskHandler.poolSize() != size) {
      taskHandler = new TaskHandler<>(size, false);
    }
    return taskHandler;
  }

  @Nullable
  @Override
  public ClassData findClass(@Nonnull DexFilter dexFilter,
                             @Nonnull ClassFilter classFilter) {
    return Utils.findFirst(classSearch(dexFilter, classFilter, 1));
  }

  @Nonnull
  @Override
  public List<ClassData> findClasses(@Nonnull DexFilter dexFilter,
                                     @Nonnull ClassFilter classFilter, int limit) {
    return classSearch(dexFilter, classFilter, limit);
  }

  @Nullable
  @Override
  public MethodData findMethod(@Nonnull DexFilter dexFilter,
                               @Nonnull ClassFilter classFilter,
                               @Nonnull MethodFilter methodFilter) {
    return Utils.findFirst(methodSearch(dexFilter, classFilter, methodFilter, 1));
  }

  @Nonnull
  @Override
  public List<MethodData> findMethods(@Nonnull DexFilter dexFilter,
                                      @Nonnull ClassFilter classFilter,
                                      @Nonnull MethodFilter methodFilter, int limit) {
    return methodSearch(dexFilter, classFilter, methodFilter, limit);
  }

  @Override
  public void onClassResult(@Nonnull DexFilter dexFilter,
                            @Nonnull ClassFilter classFilter,
                            @Nonnull Operator<ClassData> operator) {
    boolean synth = classFilter.synthItems();
    dexOperation.onClasses(
            dexFilter, classFilter, dexClass -> operator.operate(Results.ofClass(dexClass, synth))
    );
  }

  @Override
  public void onMethodResult(@Nonnull DexFilter dexFilter,
                             @Nonnull ClassFilter classFilter,
                             @Nonnull MethodFilter methodFilter,
                             @Nonnull Operator<MethodData> operator) {
    boolean synth = classFilter.synthItems();
    dexOperation.onMethods(
            dexFilter, classFilter, methodFilter, dexMethod -> operator.operate(Results.ofMethod(dexMethod, synth))
    );
  }

  @Nonnull
  @Override
  public Map<String, List<DexItemData>> findAll(@Nonnull QueryBatch batch, int limit) {
    Map<String, List<DexItemData>> results = batch.isParallel() ? new ConcurrentHashMap<>() : new LinkedHashMap<>();
    int capacity = limit > 0 ? limit : 10;
    onQueryResult(batch, (key, item) -> {
      if (limit == 1) {
        results.put(key, Collections.singletonList(item));
        return true;
      }
      List<DexItemData> current = results.computeIfAbsent(key, k -> new ArrayList<>(capacity));
      current.add(item);
      return (limit > 0 && current.size() >= limit);
    });
    return results;
  }

  @Override
  public void onQueryResult(@Nonnull QueryBatch batch,
                            @Nonnull KOperator<DexItemData> operator) {
    if (batch.isParallel()) {
      runInParallel(batch, new QueryTaskFactory(this, operator));
    } else {
      QueryTaskFactory factory = new QueryTaskFactory(this, operator);
      batch.getQueries().forEach(query -> factory.newTask(query).call());
    }
  }

  private synchronized void runInParallel(QueryBatch batch,
                                          QueryTaskFactory factory) {
    TaskHandler<Object> taskHandler = getTaskHandler(batch.threadCount());
    batch.getQueries().forEach(query -> {
      QueryTask task = factory.newTask(query);
      taskHandler.dispatch(task);
    });
    taskHandler.awaitCompletion();
  }

  private List<ClassData> classSearch(DexFilter dexFilter,
                                      ClassFilter classFilter, int limit) {
    boolean synth = classFilter.synthItems();
    List<ClassData> results = new ArrayList<>();
    dexOperation.onClasses(dexFilter, classFilter, dexClass -> {
      results.add(Results.ofClass(dexClass, synth));
      return (limit > 0 && results.size() >= limit);
    });
    return results;
  }

  private List<MethodData> methodSearch(DexFilter dexFilter,
                                        ClassFilter classFilter,
                                        MethodFilter methodFilter, int limit) {
    boolean synth = classFilter.synthItems();
    List<MethodData> results = new ArrayList<>();
    AtomicReference<ClassData> shared = new AtomicReference<>();
    dexOperation.onMethods(dexFilter, classFilter, methodFilter, dexMethod -> {
      MethodData method = Results.ofMethod(shared.get(), dexMethod, synth);
      shared.set(method.getClassData());
      results.add(method);
      return (limit > 0 && results.size() >= limit);
    });
    return results;
  }
}
