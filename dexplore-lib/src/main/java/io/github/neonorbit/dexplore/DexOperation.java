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

import io.github.neonorbit.dexplore.filter.ClassFilter;
import io.github.neonorbit.dexplore.filter.DexFilter;
import io.github.neonorbit.dexplore.filter.MethodFilter;
import io.github.neonorbit.dexplore.util.DexLog;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedMethod;

import javax.annotation.Nonnull;

final class DexOperation {
  private final DexDecoder dexDecoder;
  private final DexContainer dexContainer;

  DexOperation(String path, DexOptions options) {
    this.dexDecoder = new DexDecoder(options);
    this.dexContainer = new DexContainer(path, options);
  }

  public void onDexFiles(@Nonnull DexFilter dexFilter,
                         @Nonnull Enumerator<DexBackedDexFile> enumerator) {
    LazyDecoder<DexEntry> decoder = dexDecoder::decode;
    try {
      for (DexEntry entry : dexContainer.getEntries(dexFilter.preferredDexName)) {
        if (dexFilter.verify(entry, decoder)) {
          DexBackedDexFile dexFile = entry.getDexFile();
          DexLog.d("Processing Dex: " + entry.getDexName());
          if (enumerator.next(dexFile)) return;
        }
      }
    } catch (AbortException ignore) {}
  }

  public void onClasses(@Nonnull DexFilter dexFilter,
                        @Nonnull ClassFilter classFilter,
                        @Nonnull Enumerator<DexBackedClassDef> enumerator) {
    boolean unique = classFilter.isUnique();
    LazyDecoder<DexBackedClassDef> decoder = dexDecoder::decode;
    onDexFiles(dexFilter, dexFile -> {
      try {
        for (DexBackedClassDef dexClass : dexFile.getClasses()) {
          if (!AccessFlags.SYNTHETIC.isSet(dexClass.getAccessFlags()) &&
               classFilter.verify(dexClass, decoder)) {
            if (enumerator.next(dexClass) || unique) {
              return true;
            }
          }
        }
      } catch (AbortException e) {
        DexLog.d("Aborted: " + e.getMessage());
        return true;
      }
      return false;
    });
  }

  public void onMethods(@Nonnull DexFilter dexFilter,
                        @Nonnull ClassFilter classFilter,
                        @Nonnull MethodFilter methodFilter,
                        @Nonnull Enumerator<DexBackedMethod> enumerator) {
    LazyDecoder<DexBackedMethod> decoder = dexDecoder::decode;
    onClasses(dexFilter, classFilter, dexClass -> {
      try {
        for (DexBackedMethod dexMethod : dexClass.getMethods()) {
          if (!AccessFlags.SYNTHETIC.isSet(dexMethod.accessFlags) &&
               methodFilter.verify(dexMethod, decoder)) {
            if (enumerator.next(dexMethod)) return true;
            if (methodFilter.isUnique()) break;
          }
        }
      } catch (AbortException e) {
        DexLog.d("Aborted: " + e.getMessage());
      }
      return false;
    });
  }
}
