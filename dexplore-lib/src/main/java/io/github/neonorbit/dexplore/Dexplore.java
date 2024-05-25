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
import io.github.neonorbit.dexplore.util.DexHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * A dex explorer for locating classes and methods within dex files.
 * <p>
 *   Use {@link DexFactory} to load dex files and create Dexplore instances.
 * </p>
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public interface Dexplore {
  /**
   * Finds a dex class using the specified filter.
   * <p>The search operation stops as soon as it finds a match.</p>
   *
   * @param classFilter filter to select the desired dex class
   * @return the first matching class or null if no match is found
   * @see #findClass(DexFilter, ClassFilter)
   */
  @Nullable
  default ClassData findClass(@Nonnull ClassFilter classFilter) {
    return findClass(DexFilter.MATCH_ALL, classFilter);
  }

  /**
   * Finds a class using the specified filters.
   * <p>The search operation stops as soon as it finds a match.</p>
   *
   * @param dexFilter filter to select the desired dex files
   * @param classFilter filter to select the desired dex class
   * @return the first matching class or null if no match is found
   * @see #findClasses(DexFilter, ClassFilter, int)
   */
  @Nullable
  ClassData findClass(@Nonnull DexFilter dexFilter, @Nonnull ClassFilter classFilter);

  /**
   * Finds all classes matching the specified filters.
   * <p>The search operation stops once the specified limit is reached.</p>
   * <p>Note: The dex filter can be ignored by passing the {@link DexFilter#MATCH_ALL} instance.</p>
   *
   * @param dexFilter filter to select the desired dex files
   * @param classFilter filter to select the desired dex classes
   * @param limit the maximum number of results to return or -1 if no limit
   * @return a list of matching classes or an empty list if nothing matches
   * @see #findClass(DexFilter, ClassFilter)
   */
  @Nonnull
  List<ClassData> findClasses(@Nonnull DexFilter dexFilter,
                              @Nonnull ClassFilter classFilter, int limit);

  /**
   * Finds a dex method using the specified filters.
   * <p>The search operation stops as soon as it finds a match.</p>
   *
   * @param classFilter filter to select the desired dex classes
   * @param methodFilter filter to select the desired dex method
   * @return the first matching method or null if no match is found
   * @see #findMethod(DexFilter, ClassFilter, MethodFilter)
   */
  @Nullable
  default MethodData findMethod(@Nonnull ClassFilter classFilter,
                                @Nonnull MethodFilter methodFilter) {
    return findMethod(DexFilter.MATCH_ALL, classFilter, methodFilter);
  }

  /**
   * Finds a dex method using the specified filters.
   * <p>The search operation stops as soon as it finds a match.</p>
   * <p>Note: The dex filter can be ignored by passing the {@link DexFilter#MATCH_ALL} instance.</p>
   *
   * @param dexFilter filter to select the desired dex files
   * @param classFilter filter to select the desired dex classes
   * @param methodFilter filter to select the desired dex method
   * @return the first matching method or null if no match is found
   * @see #findMethods(DexFilter, ClassFilter, MethodFilter, int)
   */
  @Nullable
  MethodData findMethod(@Nonnull DexFilter dexFilter,
                        @Nonnull ClassFilter classFilter,
                        @Nonnull MethodFilter methodFilter);

  /**
   * Finds all methods matching the specified filters.
   * <p>The search operation stops once the specified limit is reached.</p>
   * <p>Note: The dex filter can be ignored by passing the {@link DexFilter#MATCH_ALL} instance.</p>
   *
   * @param dexFilter filter to select the desired dex files
   * @param classFilter filter to select the desired dex classes
   * @param methodFilter filter to select the desired dex methods
   * @param limit the maximum number of results to return or -1 if no limit
   * @return a list of matching methods or an empty list if nothing matches
   * @see #findMethod(DexFilter, ClassFilter, MethodFilter)
   */
  @Nonnull
  List<MethodData> findMethods(@Nonnull DexFilter dexFilter,
                               @Nonnull ClassFilter classFilter,
                               @Nonnull MethodFilter methodFilter, int limit);

  /**
   * Finds all classes matching the specified filters.
   * <p>
   *   The specified {@link Operator#operate(Object) callback} method gets called for each result.
   *   Returning {@code true} from the callback stops the search operation immediately.
   * </p>
   *
   * @param dexFilter filter to select the desired dex files
   * @param classFilter filter to select the desired dex classes
   * @param operator callback for consuming the search results
   * @see #onMethodResult(DexFilter, ClassFilter, MethodFilter, Operator) onMethodResult()
   */
  void onClassResult(@Nonnull DexFilter dexFilter,
                     @Nonnull ClassFilter classFilter,
                     @Nonnull Operator<ClassData> operator);

  /**
   * Finds all methods matching the specified filters.
   * <p>
   *   The specified {@link Operator#operate(Object) callback} method gets called for each result.
   *   Returning {@code true} from the callback stops the search operation immediately.
   * </p>
   *
   * @param dexFilter filter to select the desired dex files
   * @param classFilter filter to select the desired dex classes
   * @param methodFilter filter to select the desired dex methods
   * @param operator callback for consuming the search results
   * @see #onClassResult(DexFilter, ClassFilter, Operator) onClassResult()
   */
  void onMethodResult(@Nonnull DexFilter dexFilter,
                      @Nonnull ClassFilter classFilter,
                      @Nonnull MethodFilter methodFilter,
                      @Nonnull Operator<MethodData> operator);

  /**
   * Performs a dex search on a {@linkplain QueryBatch batch} of queries.
   * <p>
   * <b>Note:</b> Queries with no matching results are excluded from the resulting map.
   * <br>
   * <b>Note:</b> Element order is guaranteed in the resulting map, unless the batch is parallel.
   * <br>
   * <b>Note:</b> If a key exists in the map, its associated list is guaranteed to have at least one item.
   * <p> See helper methods: <br>
   * * {@link DexHelper#getFirstMatchingResults(Dexplore, QueryBatch)
   *          DexHelper.getFirstMatchingResults()
   * } <br>
   * * {@link DexHelper#getSingleMatchingResults(Dexplore, QueryBatch)
   *          DexHelper.getSingleMatchingResults()
   * }
   * @param batch a batch of dex queries
   * @param limit maximum result limit for each query or -1 if no limit
   * @return a map containing all the matching results from each query
   * @see #onQueryResult(QueryBatch, KOperator) onQueryResult(batch, callback)
   */
  @Nonnull
  Map<String, List<DexItemData>> findAll(@Nonnull QueryBatch batch, int limit);

  /**
   * Performs a dex search on a {@linkplain QueryBatch batch} of queries.
   * <p>
   * The specified {@link KOperator#operate(String, Object) callback} method gets called for each result.
   * Returning {@code true} for a given key immediately stops the search operation for the corresponding query
   * and proceeds to the next query.
   * <p>
   * <b>Note:</b> Callbacks are ignored for queries with no matching results.
   * <br>
   * <b>Note:</b> The callback order is guaranteed, unless the batch is parallel.
   * <br>
   * <b>Note:</b> The resulting {@link DexItemData} objects should be cast into appropriate subclass objects.
   * <br>
   * <b>Note:</b> By enabling the {@link QueryBatch.Builder#setParallel(boolean) parallel} option,
   * you can potentially improve search speed. However, in that case, callbacks are invoked in separate threads.
   * If you intend to add results to a {@code collection}, please ensure it is thread-safe.
   *
   * @param batch a batch of dex queries
   * @param operator a callback for consuming the search results
   * @see #findAll(QueryBatch, int) Dexplore.findAll(batch, limit)
   */
  void onQueryResult(@Nonnull QueryBatch batch, @Nonnull KOperator<DexItemData> operator);
}
