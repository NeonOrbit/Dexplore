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

import io.github.neonorbit.dexplore.exception.DexException;
import io.github.neonorbit.dexplore.iface.Internal;
import io.github.neonorbit.dexplore.util.DexLog;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.MultiDexContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Internal
final class DexContainer {
  private final boolean rootDexOnly;
  private volatile List<DexEntry> dexEntries;
  private final MultiDexContainer<DexBackedDexFile> internal;

  DexContainer(String path, DexOptions options) {
    this(loadDexContainer(path, options), options);
  }

  DexContainer(byte[] buffer, DexOptions options) {
    this(InMemoryDex.load(buffer, options.opcodes), options);
  }

  DexContainer(MultiDexContainer<DexBackedDexFile> container, DexOptions options) {
    this.internal = container;
    this.rootDexOnly = options.rootDexOnly;
  }

  @Nonnull
  public List<DexEntry> getEntries() {
    return new ArrayList<>(getDexEntries());
  }

  @Nonnull
  public List<DexEntry> getEntries(@Nullable List<String> preferred) {
    List<DexEntry> dexEntries = getEntries();
    if (preferred != null) {
      dexEntries.sort(Comparator.comparing(e -> {
        if (preferred.contains(e.getDexName()))
          return preferred.indexOf(e.getDexName());
        return preferred.size();
      }));
    }
    return dexEntries;
  }

  synchronized DexBackedDexFile loadDexFile(String dexName) {
    DexLog.d("Loading: " + dexName);
    MultiDexContainer.DexEntry<? extends DexBackedDexFile> entry;
    try {
      entry = internal.getEntry(dexName);
      if (entry == null) {
        throw new DexException("Couldn't find dex entry: " + dexName);
      }
    } catch (IOException e) {
      throw new DexException("Failed to load dex file: " + dexName, e);
    }
    return entry.getDexFile();
  }

  private List<DexEntry> getDexEntries() {
    if (this.dexEntries == null) {
      synchronized (this) {
        if (this.dexEntries == null) {
          ArrayList<DexEntry> entries = new ArrayList<>();
          try {
            for (String dexName : internal.getDexEntryNames()) {
              entries.add(new DexEntry(this, dexName));
            }
            sortDexEntries(entries);
          } catch (IOException e) {
            throw new DexException("Failed to load dex entries", e);
          }
          this.dexEntries = Collections.unmodifiableList(entries);
        }
      }
    }
    return this.dexEntries;
  }

  private void sortDexEntries(ArrayList<DexEntry> entries) {
    if (!rootDexOnly) return;
    entries.sort(Comparator.comparingInt(o -> {
      try {
        return Integer.parseInt(o.getDexName().replaceAll("\\D", ""));
      } catch (NumberFormatException ignore) {
        return -1;
      }
    }));
  }

  @SuppressWarnings("unchecked")
  private static MultiDexContainer<DexBackedDexFile> loadDexContainer(String path, DexOptions opt) {
    MultiDexContainer<? extends DexBackedDexFile> container;
    try {
      File file = new File(path);
      container = FastContainer.load(file, opt.opcodes.get(), opt.rootDexOnly);
      if (container == null) {
        container = DexFileFactory.loadDexContainer(file, opt.opcodes.get());
      }
    } catch (IOException e) {
      throw new DexException("Failed to load dex container", e);
    }
    return (MultiDexContainer<DexBackedDexFile>) container;
  }
}
