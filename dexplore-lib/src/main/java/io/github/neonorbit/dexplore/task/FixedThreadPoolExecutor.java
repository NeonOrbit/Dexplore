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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.SECONDS;

final class FixedThreadPoolExecutor extends ThreadPoolExecutor {
  private final TaskGuard taskGuard;

  public FixedThreadPoolExecutor(int size, TaskGuard taskGuard) {
    super(size, size, 5L, SECONDS, new LinkedBlockingQueue<>(), new DaemonThreadFactory());
    this.allowCoreThreadTimeOut(true);
    this.taskGuard = taskGuard;
  }

  @Override
  protected void beforeExecute(Thread t, Runnable r) {
    try {
      taskGuard.pass();
    } catch (InterruptedException e) {
      t.interrupt();
    }
    super.beforeExecute(t, r);
  }
}
