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

import com.beust.jcommander.JCommander;
import io.github.neonorbit.dexplore.filter.ClassFilter;
import io.github.neonorbit.dexplore.filter.DexFilter;
import io.github.neonorbit.dexplore.filter.MethodFilter;
import io.github.neonorbit.dexplore.result.MethodData;

public class CommandLine extends JCommander {
  public static void main(String[] args) {
    String path = args[0];
    String clazz = args[1];
    String method = args[2];
    Dexplore dexplore = Dexplore.of(path);
    MethodData data = dexplore.findMethod(DexFilter.none(),
                                          ClassFilter.ofClass(clazz),
                                          MethodFilter.ofMethod(method));
    System.out.println(data);
  }
}
