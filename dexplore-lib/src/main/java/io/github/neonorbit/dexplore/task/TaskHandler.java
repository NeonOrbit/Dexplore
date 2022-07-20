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
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * An instance of this class should not be used from multiple threads.
 * Only {@link #pause()} and {@link #resume()} are thread safe.
 *
 * <p>Note: This is an internal API.</p>
 *
 * @author NeonOrbit
 */
@Internal
public final class TaskHandler<V> {
  private int   total, completed;
  private final TaskGuard taskGuard;
  private final ThreadPoolExecutor boundedThreads;
  private final CompletionService<V> taskDispatcher;
  private final ArrayList<Future<V>> submittedTasks;
  private final BlockingQueue<Future<V>> completedTasks;

  public TaskHandler() {
    this(getIdealThreadPoolSize(), false);
  }

  public TaskHandler(int poolSize, boolean pauseSupport) {
    this.taskGuard = TaskGuard.newGuard(pauseSupport);
    this.submittedTasks = new ArrayList<>();
    this.completedTasks = new LinkedBlockingQueue<>();
    this.boundedThreads = new FixedThreadPoolExecutor(poolSize, taskGuard);
    this.taskDispatcher = new ExecutorCompletionService<>(boundedThreads, completedTasks);
  }

  public void pause() { taskGuard.lock(); }

  public void resume() { taskGuard.unlock(); }

  /**
   * @param task submit a task for execution.
   */
  public void dispatch(@Nonnull Callable<V> task) {
    dispatchAndUpdate(task);
  }

  public V retrieve() throws InterruptedException, ExecutionException {
    try {
      return retrieveAndUpdate().get();
    } catch (CancellationException ignore) {
      return null;
    }
  }

  public void forEachResult(@Nonnull Receiver<V> receiver) {
    try {
      while (hasTask()) {
        V res = retrieve();
        taskGuard.hold();
        try { if (receiver.accept(res)) {
            terminate(false); break;
          }
        } finally { taskGuard.release(); }
      }
    } catch (Exception e) {
      handleException(e);
    }
  }

  public void awaitCompletion() {
    forEachResult((r) -> false);
  }

  public void awaitCompletion(long interval, Listener listener) {
    try {
      synchronized (this) {
        while (hasTask()) {
          this.wait(interval);
          updateInternally();
          taskGuard.hold();
          listener.progress(completed, total);
          taskGuard.release();
        }
      }
    } catch (Exception e) {
      handleException(e);
    }
  }

  public boolean hasTask() { return completed < total; }

  private void dispatchAndUpdate(Callable<V> task) {
    submittedTasks.add(taskDispatcher.submit(task));
    total++;
  }

  private Future<V> retrieveAndUpdate() throws InterruptedException {
    Future<V> next = taskDispatcher.take();
    submittedTasks.remove(next);
    completed++;
    return next;
  }

  private void updateInternally() throws ExecutionException, InterruptedException {
    for (Future<V> next; (next = taskDispatcher.poll()) != null;) {
      completed++;
      submittedTasks.remove(next);
      if (next.isDone()) next.get();
    }
  }

  private void handleException(Exception exception) {
    terminate(true);
    if (exception instanceof RuntimeException) {
      throw (RuntimeException) exception;
    } else {
      throw new DexException(exception);
    }
  }

  private void terminate(boolean failed) {
    submittedTasks.forEach(task -> task.cancel(true));
    if (failed) {
      boundedThreads.shutdownNow();
    }
    while (hasTask()) {
      try {
        updateInternally();
      } catch (Exception ignore) {}
    }
    total = completed = 0;
    submittedTasks.clear();
    completedTasks.clear();
  }

  public boolean isDirty() {
    return boundedThreads.isShutdown() || hasTask();
  }

  private static int getIdealThreadPoolSize() {
    return Math.max((Runtime.getRuntime().availableProcessors() - 1), 2);
  }

  @Internal
  public interface Receiver<V> {
    boolean accept(V result);
  }

  @Internal
  public interface Listener {
    void progress(int completed, int total);
  }
}
