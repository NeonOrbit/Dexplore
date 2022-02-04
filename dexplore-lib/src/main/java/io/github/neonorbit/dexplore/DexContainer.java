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

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.MultiDexContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class DexContainer {
  private final String path;
  private final boolean rootDexOnly;
  private final DexOpcodes opcodes;
  private List<DexEntry> dexEntries;
  private MultiDexContainer<? extends DexBackedDexFile> container;

  DexContainer(String path, DexOptions options) {
    this.path = path;
    this.opcodes = options.opcodes;
    this.rootDexOnly = options.rootDexOnly;
  }

  @Nullable
  public DexEntry getEntry(String name) {
    if (name == null || name.isEmpty()) return null;
    return getDexEntries().stream()
                          .filter(e -> e.matches(name))
                          .findFirst().orElse(null);
  }

  @Nonnull
  public List<DexEntry> getEntries() {
    return new ArrayList<>(getDexEntries());
  }

  @Nonnull
  public List<DexEntry> getEntries(@Nullable String preferred) {
    DexEntry dexEntry;
    List<DexEntry> dexEntries = getEntries();
    if (dexEntries.size() > 1 && ((dexEntry = getEntry(preferred)) != null)) {
      int index = dexEntries.indexOf(dexEntry);
      if (index > 0 && dexEntries.remove(dexEntry)) {
        dexEntries.add(0, dexEntry);
      }
    }
    return dexEntries;
  }

  DexBackedDexFile loadDexFile(String dexName) throws IOException {
    MultiDexContainer.DexEntry<? extends DexBackedDexFile> entry;
    entry = getContainer().getEntry(dexName);
    if (entry == null) {
      throw new DexException("Couldn't find dex entry: " + dexName);
    }
    return entry.getDexFile();
  }

  private List<DexEntry> getDexEntries() {
    if (this.dexEntries == null) {
      this.dexEntries = new ArrayList<>();
      try {
        for (String dexName : getContainer().getDexEntryNames()) {
          this.dexEntries.add(new DexEntry(this, dexName));
        }
        this.dexEntries.sort(null);
      } catch (IOException e) {
        throw new DexException("Failed to load dex entries", e);
      }
    }
    return this.dexEntries;
  }

  private MultiDexContainer<? extends DexBackedDexFile> getContainer() {
    if (this.container == null) {
      try {
        File file = new File(this.path);
        this.container = FastContainer.load(file, opcodes.get(), rootDexOnly);
        if (this.container == null) {
          this.container = DexFileFactory.loadDexContainer(file, opcodes.get());
        }
      } catch (IOException e) {
        throw new DexException("Failed to load dex container", e);
      }
    }
    return this.container;
  }
}
