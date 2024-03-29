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

package io.github.neonorbit.dexplore.filter;

import io.github.neonorbit.dexplore.LazyDecoder;
import io.github.neonorbit.dexplore.ReferencePool;
import io.github.neonorbit.dexplore.iface.Internal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

abstract class BaseFilter<T> {
  private final boolean skip;
  protected final boolean unique;
  protected final ReferenceTypes types;
  protected final ReferenceFilter filter;

  protected BaseFilter(Builder<?,?> builder, boolean unique) {
    this.skip = shouldSkip(builder);
    this.types = builder.types;
    this.filter = builder.filter;
    this.unique = unique;
  }

  @Internal
  public boolean isUnique() {
    return unique;
  }

  @Internal
  public boolean verify(@Nonnull T dexItem, @Nonnull LazyDecoder<T> decoder) {
    return skip || filter.accept(decoder.decode(dexItem, types));
  }

  private static boolean shouldSkip(Builder<?,?> builder) {
    return builder.types == null || builder.filter == null || builder.types.hasNone();
  }

  protected static abstract class Builder<B extends Builder<B,?>, T extends BaseFilter<?>> {
    protected ReferenceTypes types;
    protected ReferenceFilter filter;

    protected Builder() {}

    protected Builder(T instance) {
      this.types = instance.types;
      this.filter = instance.filter;
    }

    protected boolean isDefault() {
      return types == null && filter == null;
    }

    protected abstract B getThis();

    public abstract T build();

    /**
     * Specify the types of references to include in the {@link ReferencePool}
     * when building it to be used with the {@link ReferenceFilter}.
     *
     * @param types a {@code ReferenceTypes} instance
     * @return {@code this} builder
     * @see #setReferenceFilter(ReferenceFilter)
     */
    public B setReferenceTypes(@Nullable ReferenceTypes types) {
      this.types = types;
      return getThis();
    }

    /**
     * Set a {@code ReferenceFilter} to apply to each item, determining whether it should be matched.
     *
     * @param filter a {@code ReferenceFilter} instance
     * @return {@code this} builder
     * @throws IllegalStateException if {@code ReferenceTypes} was not specified
     * @see #setReferenceTypes(ReferenceTypes)
     */
    public B setReferenceFilter(@Nullable ReferenceFilter filter) {
      if (this.types == null) {
        throw new IllegalStateException("ReferenceTypes was not specified");
      }
      this.filter = filter;
      return getThis();
    }
  }
}
