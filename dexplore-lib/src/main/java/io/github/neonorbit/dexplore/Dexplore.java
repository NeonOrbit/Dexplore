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

import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedMethod;

import java.lang.reflect.Method;

public class Dexplore {
  public static void main(String[] args) {
    String path = args[0];
    String clazz = args[1];
    String method = args[2];
    DexContainer container = new DexContainer(path, DexOptions.getOptimized());
    for (DexEntry entry : container.getEntries()) {
      System.out.println(entry.getDexName());
      for (DexBackedClassDef classDef: entry.getDexFile().getClasses()) {
        if (!classDef.getType().equals(clazz)) continue;
        for (DexBackedMethod dexMethod: classDef.getMethods()) {
          if (dexMethod.getName().equals(method)) {
            System.out.println(dexMethod);
            return;
          }
        }
      }
    }
  }

  public Method find() {
    try {
      return Object.class.getDeclaredMethod("toString");
    } catch (NoSuchMethodException e) {
      return null;
    }
  }
}
