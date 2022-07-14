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
import io.github.neonorbit.dexplore.result.ClassData;
import io.github.neonorbit.dexplore.result.DexItemData;
import io.github.neonorbit.dexplore.result.MethodData;
import io.github.neonorbit.dexplore.iface.KOperator;
import io.github.neonorbit.dexplore.iface.Operator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * A dex explorer for finding classes and methods from dex files.
 * <p>
 *   Use {@link DexFactory} to load dex files.
 * </p>
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public interface Dexplore {
  /**
   * Find a class using the given filters.
   * <p>
   *   The search operation will stop as soon as it finds a match.
   * </p>
   *
   * @param dexFilter a filter to select the desired dex files
   * @param classFilter a filter to find the desired dex class
   * @return the first matching class or null if no match is found
   * @see #findClasses(DexFilter, ClassFilter, int)
   */
  @Nullable
  ClassData findClass(@Nonnull DexFilter dexFilter,
                      @Nonnull ClassFilter classFilter);

  /**
   * Find all the classes that match the specified filters.
   * <p>
   *   The search operation will stop once the specified limit is reached.
   * </p>
   *
   * @param dexFilter a filter to select the desired dex files
   * @param classFilter a filter to find the desired dex classes
   * @param limit the maximum number of results to return or -1 if no limit
   * @return a list of matching classes or an empty list if nothing matches
   * @see #findClass(DexFilter, ClassFilter)
   */
  @Nonnull
  List<ClassData> findClasses(@Nonnull DexFilter dexFilter,
                              @Nonnull ClassFilter classFilter, int limit);

  /**
   * Find a method using the given filters.
   * <p>
   *   The search operation will stop as soon as it finds a match.
   * </p>
   *
   * @param dexFilter a filter to select the desired dex files
   * @param classFilter a filter to select the desired dex classes
   * @param methodFilter a filter to find the desired dex method
   * @return the first matching method or null if no match is found
   * @see #findMethods(DexFilter, ClassFilter, MethodFilter, int)
   */
  @Nullable
  MethodData findMethod(@Nonnull DexFilter dexFilter,
                        @Nonnull ClassFilter classFilter,
                        @Nonnull MethodFilter methodFilter);

  /**
   * Find all the methods that match the specified filters.
   * <p>
   *   The search operation will stop once the specified limit is reached.
   * </p>
   *
   * @param dexFilter a filter to select the desired dex files
   * @param classFilter a filter to select the desired dex classes
   * @param methodFilter a filter to find the desired dex methods
   * @param limit the maximum number of results to return or -1 if no limit
   * @return a list of matching classes or an empty list if nothing matches
   * @see #findMethod(DexFilter, ClassFilter, MethodFilter)
   */
  @Nonnull
  List<MethodData> findMethods(@Nonnull DexFilter dexFilter,
                               @Nonnull ClassFilter classFilter,
                               @Nonnull MethodFilter methodFilter, int limit);

  /**
   * Find all the classes that match the specified filters.
   * <p>
   *   The specified callback {@link Operator#operate(Object) operate(item)}
   *   will be called for each result.
   *   Return {@code true} when you want to stop.
   * </p>
   *
   * @param dexFilter a filter to select the desired dex files
   * @param classFilter a filter to find the desired dex classes
   * @param operator a callback for consuming the search results
   */
  void onClassResult(@Nonnull DexFilter dexFilter,
                     @Nonnull ClassFilter classFilter,
                     @Nonnull Operator<ClassData> operator);

  /**
   * Find all the methods that match the specified filters.
   * <p>
   *   The specified callback {@link Operator#operate(Object) operate(item)}
   *   will be called for each result.
   *   Return {@code true} when you want to stop.
   * </p>
   *
   * @param dexFilter a filter to select the desired dex files
   * @param classFilter a filter to select the desired dex classes
   * @param methodFilter a filter to find the desired dex methods
   * @param operator a callback for consuming the search results
   */
  void onMethodResult(@Nonnull DexFilter dexFilter,
                      @Nonnull ClassFilter classFilter,
                      @Nonnull MethodFilter methodFilter,
                      @Nonnull Operator<MethodData> operator);

  /**
   * Perform a batch of queries. See {@link QueryBatch}.
   * <p>
   *   The specified callback {@link KOperator#operate(String, Object) operate(key,item)}
   *   will be called for each result.
   *   Return {@code true} when you want to stop further search for a given key.
   * </p>
   * <pre>--------------------------------------------------</pre>
   * <b>Note:</b> Set {@link QueryBatch.Builder#setParallel(boolean) parallel}
   * to speed up the process. In that case, the callback will be called in a separate thread.
   * If you are adding results to a list, make sure it is thread-safe.
   * <br>
   * <b>Note:</b> If no result is found for a given key, it will be ignored silently.
   * <br>
   * <b>Note:</b> Result item will be in {@link DexItemData} form,
   * cast it to an appropriate ({@link ClassData} or {@link MethodData}) object.
   *
   * @param batch a batch of queries
   * @param operator a callback for consuming the search results
   */
  void onQueryResult(@Nonnull QueryBatch batch,
                     @Nonnull KOperator<DexItemData> operator);
}
