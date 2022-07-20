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

interface TaskGuard {
  TaskGuard DEFAULT = new TaskGuard() {
    public void hold() {}
    public void release() {}
    public void pass() {}
    public void lock() {}
    public void unlock() {}
  };

  static TaskGuard newGuard(boolean safe) {
    return safe ? new SafeGuard() : DEFAULT;
  }

  /**
   * Hold the gate and prevent it from being locked,
   * but let others {@linkplain #pass() pass} freely.
   */
  void hold();

  /**
   * Release the gate from hold.
   */
  void release();

  /**
   * Acquire the gate pass, wait if necessary.
   */
  void pass() throws InterruptedException;

  /**
   * Lock the gate and prevent others from holding it.
   */
  void lock();

  /**
   * Unlock the gate.
   */
  void unlock();
}
