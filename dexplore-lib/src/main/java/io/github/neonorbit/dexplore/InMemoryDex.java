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
import io.github.neonorbit.dexplore.exception.UnsupportedFileException;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedDexFile.NotADexFile;
import org.jf.dexlib2.dexbacked.DexBackedOdexFile;
import org.jf.dexlib2.dexbacked.DexBackedOdexFile.NotAnOdexFile;
import org.jf.dexlib2.iface.MultiDexContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

final class InMemoryDex implements MultiDexContainer<DexBackedDexFile> {
  private static final String TAG = "InMemoryDex";

  private final String dexName;
  private final DexBackedDexFile dexFile;

  private InMemoryDex(String dexName, DexBackedDexFile dexFile) {
    this.dexName = dexName;
    this.dexFile = dexFile;
  }

  @Nonnull
  public static InMemoryDex load(@Nonnull byte[] buffer, @Nonnull DexOpcodes opcodes) {
    try {
      return new InMemoryDex(TAG, new DexBackedDexFile(opcodes.get(), buffer));
    } catch (NotADexFile ignore) {}
    try {
      return new InMemoryDex(TAG, DexBackedOdexFile.fromInputStream(
              opcodes.get(), new ByteArrayInputStream(buffer)
      ));
    } catch (IOException e) {
      throw new DexException("Failed to load dex buffer from memory", e);
    } catch (NotAnOdexFile ignored) {}
    throw new UnsupportedFileException("Not a dex or odex file");
  }

  @Nonnull
  @Override
  public List<String> getDexEntryNames() {
    return Collections.singletonList(dexName);
  }

  @Nullable
  @Override
  public DexEntry<DexBackedDexFile> getEntry(@Nonnull String entry) {
    if (!entry.equals(dexName)) {
      return null;
    }
    return new DexEntry<DexBackedDexFile>() {
      @Nonnull
      @Override
      public String getEntryName() {
        return dexName;
      }

      @Nonnull
      @Override
      public DexBackedDexFile getDexFile() {
        return dexFile;
      }

      @Nonnull
      @Override
      public MultiDexContainer<DexBackedDexFile> getContainer() {
        return InMemoryDex.this;
      }
    };
  }
}
