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

/**
 * Represents a set of dexplore options.
 * <p>
 * List of available options:
 * <ul>
 *   <li>{@link #opcodes}</li>
 *   <li>{@link #enableCache}</li>
 *   <li>{@link #rootDexOnly}</li>
 * </ul>
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public final class DexOptions {
  /**
   * Dalvik/ART Opcodes.
   * <p>
   *   <b>Default:</b> {@code auto}
   * </p>
   * @since 1.0.0
   */
  public DexOpcodes opcodes = DexOpcodes.auto();

  /**
   * Enables dexplore cache.
   * <p>
   * Useful for conducting multiple dex queries on a single dexplore instance.
   * <p>
   *   <b>Default:</b> {@code false}
   * </p>
   * @since 1.0.0
   */
  public boolean enableCache = false;

  /**
   * If enabled, only the root dex files of an apk are loaded.
   * <p>
   * Root dex files are:<br> [Classes.dex, Classes2.dex ... ClassesN.dex]
   * <p>
   *   <b>Default:</b> {@code false}
   * </p>
   * @since 1.0.0
   */
  public boolean rootDexOnly = false;

  /**
   * @return a default {@code DexOptions} instance
   */
  public static DexOptions getDefault() {
    return new DexOptions();
  }
}
