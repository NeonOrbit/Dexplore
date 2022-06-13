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

package io.github.neonorbit.dexplore.util;

/**
 * The default logger used to log info during the dex analyzing process.
 * <p>By default, the logger is disabled. Use {@link #enable()} to enable the logger.</p>
 *
 * <p>If you want to redirect the logs, set your own logger with {@link #setLogger(DexLogger)}.</p>
 *
 * Example:
 * <pre>{@code
 *   DexLog.enable();
 *   DexLog.setLogger(new DexLogger() {
 *     public void debug(String msg) {
 *       System.out.println("Dex: " + msg);
 *     }
 *     public void warn(String msg) {
 *       System.err.println("Dex: " + msg);
 *     }
 *   });
 * }</pre>
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public final class DexLog {
  private static final DexLogger DEFAULT;

  private static boolean enabled;
  private static DexLogger current;

  static {
    DEFAULT = new DexLogger() {};
    current = DEFAULT;
    enabled = false;
  }

  private DexLog() {
    throw new AssertionError();
  }

  public static void setLogger(DexLogger logger) {
    if (logger != null) {
      DexLog.current = logger;
    } else {
      resetLogger();
    }
  }

  public static void resetLogger() {
    DexLog.current = DEFAULT;
  }

  public static void enable() {
    DexLog.enabled = true;
  }

  public static void disable() {
    DexLog.enabled = false;
  }

  @Internal
  public static void d(String msg) {
    if (enabled) DexLog.current.debug(msg);
  }

  @Internal
  public static void w(String msg) {
    if (enabled) DexLog.current.warn(msg);
  }
}
