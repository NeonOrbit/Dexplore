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

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

final class DaemonThreadFactory implements ThreadFactory {
  private static final String prefix = "daemon-thread-";
  private final AtomicInteger counter;

  DaemonThreadFactory() {
    this.counter = new AtomicInteger(1);
  }

  @Override
  public Thread newThread(@Nonnull Runnable r) {
    Thread thread = new Thread(r, prefix + counter.getAndIncrement());
    if (!thread.isDaemon()) thread.setDaemon(true);
    return thread;
  }
}
