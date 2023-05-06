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
import io.github.neonorbit.dexplore.exception.AbortException;
import io.github.neonorbit.dexplore.util.DexLog;
import io.github.neonorbit.dexplore.util.DexUtils;
import io.github.neonorbit.dexplore.iface.Internal;
import io.github.neonorbit.dexplore.iface.Operator;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedMethod;

import javax.annotation.Nonnull;

@Internal
final class DexOperation {
  private final DexDecoder dexDecoder;
  private final DexContainer dexContainer;

  DexOperation(String path, DexOptions options) {
    this.dexDecoder = new DexDecoder(options);
    this.dexContainer = new DexContainer(path, options);
  }

  DexOperation(byte[] buffer, DexOptions options) {
    this.dexDecoder = new DexDecoder(options);
    this.dexContainer = new DexContainer(buffer, options);
  }

  public void onDexFiles(@Nonnull DexFilter dexFilter,
                         @Nonnull Operator<DexBackedDexFile> operator) {
    LazyDecoder<DexEntry> decoder = dexDecoder::decode;
    try {
      for (DexEntry entry : dexContainer.getEntries(dexFilter.preferredList())) {
        if (dexFilter.verify(entry, decoder)) {
          DexBackedDexFile dexFile = entry.getDexFile();
          DexLog.d("Processing: " + entry.getDexName());
          if (operator.operate(dexFile) || dexFilter.isUnique()) {
            return;
          }
        }
      }
    } catch (AbortException e) {
      if (!e.isSilent()) DexLog.w("Aborted: " + e.getMessage());
    }
  }

  public void onClasses(@Nonnull DexFilter dexFilter,
                        @Nonnull ClassFilter classFilter,
                        @Nonnull Operator<DexBackedClassDef> operator) {
    boolean unique = classFilter.isUnique();
    LazyDecoder<DexBackedClassDef> decoder = dexDecoder::decode;
    onDexFiles(dexFilter, dexFile -> {
      try {
        for (DexBackedClassDef dexClass : DexUtils.dexClasses(dexFile)) {
          if (classFilter.verify(dexClass, decoder)) {
            if (operator.operate(dexClass) || unique) {
              return true;
            }
          }
        }
      } catch (AbortException e) {
        DexLog.w("Aborted: " + e.getMessage());
        return true;
      }
      return false;
    });
  }

  public void onMethods(@Nonnull DexFilter dexFilter,
                        @Nonnull ClassFilter classFilter,
                        @Nonnull MethodFilter methodFilter,
                        @Nonnull Operator<DexBackedMethod> operator) {
    LazyDecoder<DexBackedMethod> decoder = dexDecoder::decode;
    onClasses(dexFilter, classFilter, dexClass -> {
      try {
        for (DexBackedMethod dexMethod : DexUtils.dexMethods(dexClass)) {
          if (methodFilter.verify(dexMethod, decoder)) {
            if (operator.operate(dexMethod)) return true;
            if (methodFilter.isUnique()) break;
          }
        }
      } catch (AbortException e) {
        DexLog.w("Aborted: " + e.getMessage());
      }
      return false;
    });
  }
}
