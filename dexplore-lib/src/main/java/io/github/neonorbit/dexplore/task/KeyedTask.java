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

import io.github.neonorbit.dexplore.util.DexLog;

import javax.annotation.Nonnull;
import java.util.concurrent.Callable;

abstract class KeyedTask<V> implements Callable<V> {
  private final String key;

  protected KeyedTask(@Nonnull String key) {
    this.key = key;
  }

  protected abstract V run();

  @Override
  public V call() {
    DexLog.d("Task Started: " + key);
    final V result = run();
    DexLog.d("Task Finished: " + key);
    return result;
  }
}
