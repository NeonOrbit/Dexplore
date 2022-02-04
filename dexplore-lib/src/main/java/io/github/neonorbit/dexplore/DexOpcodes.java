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

import org.jf.dexlib2.Opcodes;

/**
 * A wrapper class for {@link Opcodes}
 */
public final class DexOpcodes {
  private final Opcodes opcodes;

  private DexOpcodes(Opcodes opcodes) {
    this.opcodes = opcodes;
  }

  Opcodes get() {
    return this.opcodes;
  }

  static DexOpcodes none() {
    return new DexOpcodes(null);
  }

  /**
   * Wrapper method for {@link Opcodes#getDefault()}
   *
   * @return a default Opcodes instance
   */
  public static DexOpcodes getDefault() {
    return new DexOpcodes(Opcodes.getDefault());
  }

  public static DexOpcodes forApi(int api) {
    return new DexOpcodes(Opcodes.forApi(api));
  }

  public static DexOpcodes forArtVersion(int artVersion) {
    return new DexOpcodes(Opcodes.forArtVersion(artVersion));
  }

  public static DexOpcodes forDexVersion(int dexVersion) {
    return new DexOpcodes(Opcodes.forDexVersion(dexVersion));
  }
}
