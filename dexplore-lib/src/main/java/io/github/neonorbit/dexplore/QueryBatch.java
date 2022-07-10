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
import io.github.neonorbit.dexplore.util.Utils;

import javax.annotation.Nonnull;
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
 * @see Builder#addClassQuery(String, DexFilter, ClassFilter) addClassQuery()
 * @see Builder#addMethodQuery(String, DexFilter, ClassFilter, MethodFilter) addMethodQuery()
 *
 * @author NeonOrbit
 * @since 1.4.0
 */
public class QueryBatch {
  private final Map<String, Query> map;

  private QueryBatch(Builder builder) {
    this.map = Collections.unmodifiableMap(new HashMap<>(builder.map));
  }

  Collection<Query> getQueries() {
    return map.values();
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
   * @see #addClassQuery(String, DexFilter, ClassFilter) addClassQuery()
   * @see #addMethodQuery(String, DexFilter, ClassFilter, MethodFilter) addMethodQuery()
   */
  public static class Builder {
    private final Map<String, Query> map = new HashMap<>();

    public QueryBatch build() {
      return new QueryBatch(this);
    }

    /**
     * Add a query to find all the classes that match the specified filters.
     *
     * @param key a unique key to identify the query
     * @param dexFilter a filter to select the desired dex files
     * @param classFilter a filter to find the desired dex classes
     * @return {@code this} builder
     * @see #addMethodQuery(String, DexFilter, ClassFilter, MethodFilter)
     */
    public Builder addClassQuery(@Nonnull String key,
                                 @Nonnull DexFilter dexFilter,
                                 @Nonnull ClassFilter classFilter) {
      Utils.checkNotNull(key, dexFilter, classFilter);
      if (map.putIfAbsent(key, new ClassQuery(key, dexFilter, classFilter)) != null) {
        throw new IllegalArgumentException("A query with the given key is already added: " + key);
      }
      return this;
    }

    /**
     * Add a query to find all the methods that match the specified filters.
     *
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
      Utils.checkNotNull(key, dexFilter, classFilter, methodFilter);
      if (map.putIfAbsent(key, new MethodQuery(key, dexFilter, classFilter, methodFilter)) != null) {
        throw new IllegalArgumentException("A query with the given key is already added: " + key);
      }
      return this;
    }
  }

  static abstract class Query {
    final String key;
    final DexFilter dexFilter;

    protected Query(String key, DexFilter dexFilter) {
      this.key = key;
      this.dexFilter = dexFilter;
    }

    @Override
    public int hashCode() {
      return this.key.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      return (this == obj) || (obj instanceof Query) &&
             (this.key.equals(((Query) obj).key));
    }
  }

  static class ClassQuery extends Query {
    final ClassFilter classFilter;
    private ClassQuery(String key, DexFilter dexFilter, ClassFilter classFilter) {
      super(key, dexFilter);
      this.classFilter = classFilter;
    }
  }

  static class MethodQuery extends ClassQuery {
    final MethodFilter methodFilter;
    private MethodQuery(String key, DexFilter dexFilter, ClassFilter classFilter,
                        MethodFilter methodFilter) {
      super(key, dexFilter, classFilter);
      this.methodFilter = methodFilter;
    }
  }
}
