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
import io.github.neonorbit.dexplore.iface.Internal;
import io.github.neonorbit.dexplore.iface.Mapper;
import io.github.neonorbit.dexplore.result.ClassData;
import io.github.neonorbit.dexplore.result.DexItemData;
import io.github.neonorbit.dexplore.result.MethodData;
import io.github.neonorbit.dexplore.util.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A class representing a batch of queries.
 * <p>
 *   Note: Use {@link Builder Builder} to build a batch.
 * </p>
 * @see Builder#setParallel(boolean) setParallel()
 * @see Builder#addClassQuery(String, DexFilter, ClassFilter) addClassQuery()
 * @see Builder#addMethodQuery(String, DexFilter, ClassFilter, MethodFilter) addMethodQuery()
 *
 * @author NeonOrbit
 * @since 1.4.0
 */
public final class QueryBatch {
  private final boolean parallel;
  private final Map<String, Query> map;

  private QueryBatch(Builder builder) {
    this.parallel = builder.parallel;
    this.map = Collections.unmodifiableMap(new HashMap<>(builder.map));
  }

  Collection<Query> getQueries() {
    return map.values();
  }

  public boolean isParallel() {
    return size() > 1 && parallel;
  }

  public Set<String> getKeys() {
    return map.keySet();
  }

  /**
   * @return total number of queries
   */
  public int size() {
    return map.size();
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * QueryBatch Builder
   *
   * @see Builder#setParallel(boolean) setParallel()
   * @see #addClassQuery(String, DexFilter, ClassFilter) addClassQuery()
   * @see #addMethodQuery(String, DexFilter, ClassFilter, MethodFilter) addMethodQuery()
   */
  public static class Builder {
    private boolean parallel;
    private final Map<String, Query> map = new HashMap<>();

    public QueryBatch build() {
      return new QueryBatch(this);
    }

    /**
     * Process queries in parallel
     * @param parallel {@code true} to set, {@code false} to unset.
     * @return {@code this} builder
     */
    public Builder setParallel(boolean parallel) {
      this.parallel = parallel;
      return this;
    }

    /**
     * Add a query to find all the classes that match the specified filters.
     * @param key a unique key to identify the query
     * @param classFilter a filter to find the desired dex classes
     * @return {@code this} builder
     * @see #addClassQuery(String, DexFilter, ClassFilter)
     */
    public Builder addClassQuery(@Nonnull String key, @Nonnull ClassFilter classFilter) {
      return addClassQuery(key, DexFilter.MATCH_ALL, classFilter);
    }

    /**
     * Add a query to find all the classes that match the specified filters.
     * @param key a unique key to identify the query
     * @param dexFilter a filter to select the desired dex files
     * @param classFilter a filter to find the desired dex classes
     * @return {@code this} builder
     * @see #addMethodQuery(String, DexFilter, ClassFilter, MethodFilter)
     */
    public Builder addClassQuery(@Nonnull String key,
                                 @Nonnull DexFilter dexFilter,
                                 @Nonnull ClassFilter classFilter) {
      return addClassQuery(key, dexFilter, classFilter, null);
    }

    /**
     * Add a query to find all the classes that match the specified filters.
     * <p><br>
     *   <b>Note:</b> The mapper can be used to transform the result into a different dex item.
     *   If the mapper returns null, the result will be excluded,
     *   and the search will continue for the next match.
     *   ({@linkplain Mapper see details}).
     * </p>
     *
     * @param key a unique key to identify the query
     * @param dexFilter a filter to select the desired dex files
     * @param classFilter a filter to find the desired dex classes
     * @param mapper a mapper to transform the result into a different dex item
     * @return {@code this} builder
     * @see #addMethodQuery(String, DexFilter, ClassFilter, MethodFilter, Mapper)
     */
    public Builder addClassQuery(@Nonnull String key,
                                 @Nonnull DexFilter dexFilter,
                                 @Nonnull ClassFilter classFilter,
                                 @Nullable Mapper<ClassData> mapper) {
      Utils.checkNotNull(key, dexFilter, classFilter);
      if (map.putIfAbsent(key, new ClassQuery(key, dexFilter, classFilter, mapper)) != null) {
        throw new IllegalArgumentException("A query with the given key is already added: " + key);
      }
      return this;
    }

    /**
     * Add a query to find all the methods that match the specified filters.
     * @param key a unique key to identify the query
     * @param classFilter a filter to select the desired dex classes
     * @param methodFilter a filter to find the desired dex methods
     * @return {@code this} builder
     * @see #addMethodQuery(String, DexFilter, ClassFilter, MethodFilter)
     */
    public Builder addMethodQuery(@Nonnull String key,
                                  @Nonnull ClassFilter classFilter,
                                  @Nonnull MethodFilter methodFilter) {
      return addMethodQuery(key, DexFilter.MATCH_ALL, classFilter, methodFilter);
    }

    /**
     * Add a query to find all the methods that match the specified filters.
     * @param key a unique key to identify the query
     * @param dexFilter a filter to select the desired dex files
     * @param classFilter a filter to select the desired dex classes
     * @param methodFilter a filter to find the desired dex methods
     * @return {@code this} builder
     * @see #addClassQuery(String, DexFilter, ClassFilter)
     */
    public Builder addMethodQuery(@Nonnull String key,
                                  @Nonnull DexFilter dexFilter,
                                  @Nonnull ClassFilter classFilter,
                                  @Nonnull MethodFilter methodFilter) {
      return addMethodQuery(key, dexFilter, classFilter, methodFilter, null);
    }

  /**
   * Add a query to find all the methods that match the specified filters.
   * <p><br>
   *   <b>Note:</b> The mapper can be used to transform the result into a different dex item.
   *   If the mapper returns null, the result will be excluded,
   *   and the search will continue for the next match.
   *   ({@linkplain Mapper see details}).
   * </p>
   *
   * @param key a unique key to identify the query
   * @param dexFilter a filter to select the desired dex files
   * @param classFilter a filter to select the desired dex classes
   * @param methodFilter a filter to find the desired dex methods
   * @param mapper a mapper to transform the result into a different dex item
   * @return {@code this} builder
   * @see #addClassQuery(String, DexFilter, ClassFilter, Mapper)
   */
  public Builder addMethodQuery(@Nonnull String key,
                                @Nonnull DexFilter dexFilter,
                                @Nonnull ClassFilter classFilter,
                                @Nonnull MethodFilter methodFilter,
                                @Nullable Mapper<MethodData> mapper) {
    Utils.checkNotNull(key, dexFilter, classFilter, methodFilter);
    if (map.putIfAbsent(key, new MethodQuery(key, dexFilter, classFilter, methodFilter, mapper)) != null) {
      throw new IllegalArgumentException("A query with the given key is already added: " + key);
    }
    return this;
  }
}

  @Internal
  public static abstract class Query {
    @Nonnull public final String key;
    @Nonnull public final DexFilter dexFilter;
    private final Mapper<DexItemData> mapper;

    @SuppressWarnings("unchecked")
    protected Query(@Nonnull String key, @Nonnull DexFilter dexFilter,
                    @Nullable Mapper<? extends DexItemData> mapper) {
      this.key = key;
      this.dexFilter = dexFilter;
      this.mapper = (Mapper<DexItemData>) mapper;
    }

    public DexItemData map(@Nonnull DexItemData item) {
      return mapper == null ? item : mapper.map(item);
    }

    @Override
    public int hashCode() {
      return this.key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      return (this == obj) || (obj instanceof Query) && (this.key.equals(((Query) obj).key));
    }
  }

  @Internal
  public static class ClassQuery extends Query {
    public final ClassFilter classFilter;

    private ClassQuery(@Nonnull String key, @Nonnull DexFilter dexFilter,
                       @Nonnull ClassFilter classFilter, @Nullable Mapper<? extends DexItemData> mapper) {
      super(key, dexFilter, mapper);
      this.classFilter = classFilter;
    }
  }

  @Internal
  public static class MethodQuery extends ClassQuery {
    public final MethodFilter methodFilter;

    private MethodQuery(@Nonnull String key, @Nonnull DexFilter dexFilter, @Nonnull ClassFilter classFilter,
                        @Nonnull MethodFilter methodFilter, @Nullable Mapper<MethodData> mapper) {
      super(key, dexFilter, classFilter, mapper);
      this.methodFilter = methodFilter;
    }
  }
}
