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

import io.github.neonorbit.dexplore.iface.Internal;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;

import javax.annotation.Nonnull;

@Internal
public final class DexEntry implements Comparable<DexEntry> {
  private final String dexName;
  private final DexContainer container;
  private volatile DexBackedDexFile dexFile;

  DexEntry(DexContainer container, String dexName) {
    this.dexName = dexName;
    this.container = container;
  }

  @Nonnull
  public String getDexName() {
    return this.dexName;
  }

  @Nonnull
  public DexBackedDexFile getDexFile() {
    if (this.dexFile == null) {
      synchronized (this) {
        if (this.dexFile == null) {
          this.dexFile = container.loadDexFile(this.dexName);
        }
      }
    }
    return this.dexFile;
  }

  @Override
  public int compareTo(@Nonnull DexEntry o) {
    return this.dexName.compareTo(o.dexName);
  }

  @Override
  public int hashCode() {
    return dexName.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj instanceof DexEntry) {
      DexEntry another = (DexEntry) obj;
      return this.dexName.equals(another.dexName);
    }
    return false;
  }

  @Override
  public String toString() {
    return dexName;
  }
}
