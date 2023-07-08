/*
 * Copyright (C) 2023 NeonOrbit
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

package io.github.neonorbit.dexplore.util;

import io.github.neonorbit.dexplore.Dexplore;
import io.github.neonorbit.dexplore.QueryBatch;
import io.github.neonorbit.dexplore.filter.ClassFilter;
import io.github.neonorbit.dexplore.filter.DexFilter;
import io.github.neonorbit.dexplore.result.ClassData;
import io.github.neonorbit.dexplore.result.DexItemData;
import io.github.neonorbit.dexplore.result.FieldData;
import io.github.neonorbit.dexplore.result.MethodData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public final class DexHelper {
  /**
   * Retrieves a dex class.
   * @param dexplore a dexplore instance
   * @param fullName {@linkplain Class#getName() full name} of the class
   * @return a {@code ClassData} object of the specified classname or null
   */
  @Nullable
  public static ClassData getClass(@Nonnull Dexplore dexplore, @Nonnull String fullName) {
    return dexplore.findClass(ClassFilter.ofClass(fullName));
  }

  /**
   * Retrieves a dex class, including its inner classes.
   * @param dexplore a dexplore instance
   * @param fullName {@linkplain Class#getName() full name} of the class
   * @return a list containing the specified class and its inner classes
   */
  @Nonnull
  public static List<ClassData> getClassWithInners(@Nonnull Dexplore dexplore, @Nonnull String fullName) {
    return dexplore.findClasses(DexFilter.MATCH_ALL, ClassFilter.builder().setClassPattern(
            Pattern.compile("^\\Q" + fullName + "\\E(\\$.*)?$")
    ).build(), -1);
  }

  /**
   * Retrieves a dex method.
   * @param dexplore a dexplore instance
   * @param className full name of the declaring class
   * @param methodName name of the method
   * @param parameters parameters of the method
   * @return a {@code MethodData} object of the specified method name or null
   */
  @Nullable
  public static MethodData getMethod(@Nonnull Dexplore dexplore, @Nonnull String className,
                                     @Nonnull String methodName, @Nonnull String... parameters) {
    ClassData cls = dexplore.findClass(ClassFilter.ofClass(className));
    return cls == null ? null : cls.getMethod(methodName, parameters);
  }

  /**
   * Returns the resource id associated with the specified resource name.
   * <p>
   *   Note: You can get an instance of a resource class
   *   using the {@link #getClass(Dexplore, String) getClass()} method.
   *   Resource class must be specific, such as,
   *   <pre>  com.app.R$string, com.app.R$color  etc...</pre>
   * Example:
   * <pre>
   *   ClassData rString = DexHelper.getClass(dexplore, "com.app.R$string")
   *   Integer aStringResId = DexHelper.getResId(rString, "a_string_res_name")
   *   Integer aPageTitleId = DexHelper.getResId(rString, "app_main_page_title")
   * </pre>
   * @apiNote This method is intended to be used with
   *   {@link ClassFilter.Builder#setNumbers(Number...) ClassFilter.setNumbers()}
   *   to assist in locating classes with resource usage.
   * @param resClass {@code ClassData} instance of the resource class
   * @param resName resource name
   * @return the resource id associated with the specified resource name
   */
  @Nullable
  public static Integer getResId(@Nonnull ClassData resClass, @Nonnull String resName) {
    FieldData field = resClass.getField(resName);
    return field != null && field.type.equals("int") ? (Integer) field.getInitialValue() : null;
  }

  /**
   * Returns a map containing the first matching result of each query.
   * @param dexplore a dexplore instance
   * @param batch a batch of queries
   * @return a map containing the first matching result of each query
   */
  @Nonnull
  public static Map<String, DexItemData> getFirstMatchingResults(@Nonnull Dexplore dexplore,
                                                                 @Nonnull QueryBatch batch) {
    Map<String, DexItemData> results = newSafeMap(batch.isParallel());
    dexplore.onQueryResult(batch, (key, item) -> {
      results.put(key, item);
      return true;
    });
    return results;
  }

  /**
   * Returns a map containing the results of queries that match exactly one entry.
   * <p>
   *   Note: The map will NOT contain the results of queries that have zero or multiple matches.
   *   This is slower than {@link #getFirstMatchingResults(Dexplore, QueryBatch) getFirstMatchingResults()}.
   * </p>
   *
   * @param dexplore a dexplore instance
   * @param batch a batch of queries
   * @return a map containing the results of queries that match exactly one entry.
   */
  @Nonnull
  public static Map<String, DexItemData> getSingleMatchingResults(@Nonnull Dexplore dexplore,
                                                                  @Nonnull QueryBatch batch) {
    Map<String, DexItemData> results = newSafeMap(batch.isParallel());
    dexplore.onQueryResult(batch, (key, item) -> {
      if (results.putIfAbsent(key, item) != null) {
        results.remove(key);
        return true;
      }
      return false;
    });
    return results;
  }

  private static <K, V> Map<K, V> newSafeMap(boolean isMultiThreaded) {
    return isMultiThreaded ? new ConcurrentHashMap<>() : new HashMap<>();
  }
}
