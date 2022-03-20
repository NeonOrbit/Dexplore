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

public final class DexOptions {
  public boolean enableCache = false;
  public boolean rootDexOnly = false;
  public DexOpcodes opcodes = DexOpcodes.none();

  public static DexOptions getDefault() {
    return new DexOptions();
  }

  public static DexOptions getOptimized() {
    DexOptions options = getDefault();
    options.rootDexOnly = true;
    return options;
  }
}
