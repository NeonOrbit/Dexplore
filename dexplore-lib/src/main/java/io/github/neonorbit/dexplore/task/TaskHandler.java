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

import io.github.neonorbit.dexplore.exception.DexException;
import io.github.neonorbit.dexplore.iface.Internal;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Internal
public final class TaskHandler<V> {
  private final ExecutorService           boundedThreads;
  private final CompletionService<V>      executorService;
  private final ArrayList<Future<V>>      submittedTasks;
  private final BlockingQueue<Future<V>>  completedTasks;

  @Internal
  public TaskHandler() {
    this.submittedTasks  = new ArrayList<>();
    this.completedTasks  = new LinkedBlockingQueue<>();
    this.boundedThreads  = Executors.newFixedThreadPool(poolSize(), new DaemonThreadFactory());
    ((ThreadPoolExecutor)  boundedThreads).setKeepAliveTime(5, TimeUnit.SECONDS);
    ((ThreadPoolExecutor)  boundedThreads).allowCoreThreadTimeOut(true);
    this.executorService = new ExecutorCompletionService<>(boundedThreads, completedTasks);
  }

  public void submit(@Nonnull Callable<V> task) {
    submittedTasks.add(executorService.submit(task));
  }

  public V next() throws InterruptedException, ExecutionException {
    Future<V> next = executorService.take();
    submittedTasks.remove(next);
    try {
      return next.get();
    } catch (CancellationException ignore) {
      return null;
    }
  }

  public void forEachResult(@Nonnull Result<V> result) {
    try {
      while (hasTask()) {
        try {
          if (result.accept(next())) break;
        } catch (InterruptedException | ExecutionException e) {
          throw new DexException(e);
        }
      }
    } finally {
      terminate();
    }
  }

  public void awaitCompletion() {
    forEachResult((r) -> false);
  }

  public boolean hasTask() {
    return !submittedTasks.isEmpty();
  }

  public void terminate() {
    submittedTasks.forEach(task -> task.cancel(true));
    clear();
  }

  public void shutdown() {
    boundedThreads.shutdownNow();
    clear();
  }

  private void clear() {
    submittedTasks.clear();
    completedTasks.clear();
  }

  private int poolSize() {
    return Math.max((Runtime.getRuntime().availableProcessors() - 1), 2);
  }

  @Internal
  public interface Result<V> {
    boolean accept(V result);
  }
}
