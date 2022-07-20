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

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

final class SafeGuard implements TaskGuard {
  private boolean allowed = true;
  private final ReentrantLock internal = new ReentrantLock();
  private final ReentrantLock external = new ReentrantLock();
  private final Condition permission = internal.newCondition();

  @Override
  public void hold() {
    external.lock();
  }

  @Override
  public void release() {
    external.unlock();
  }

  @Override
  public void pass() throws InterruptedException {
    internal.lock();
    try {
      while (!allowed) {
        permission.await();
      }
    } finally {
      internal.unlock();
    }
  }

  @Override
  public void lock() {
    internal.lock();
    try {
      allowed = false;
      external.lock();
    } finally {
      internal.unlock();
    }
  }

  @Override
  public void unlock() {
    internal.lock();
    try {
      if (!allowed) {
        allowed = true;
        external.unlock();
        permission.signalAll();
      }
    } finally {
      internal.unlock();
    }
  }
}
