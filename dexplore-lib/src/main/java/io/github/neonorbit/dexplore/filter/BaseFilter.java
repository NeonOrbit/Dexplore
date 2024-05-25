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
     * Determines which types of references should be added in the {@link ReferencePool} objects
     * for use with the {@linkplain #setReferenceFilter(ReferenceFilter) reference filter}.
     *
     * @param types the {@code ReferenceTypes} instance
     * @return {@code this} builder
     * @see #setReferenceFilter(ReferenceFilter)
     */
    public B setReferenceTypes(@Nullable ReferenceTypes types) {
      this.types = types;
      return getThis();
    }

    /**
     * Set a {@code ReferenceFilter} to apply to the {@link ReferencePool} of each item,
     * determining whether the item should be matched.
     *
     * <p><b>Note:</b> The ReferenceFilter is expensive and is applied after all other conditions.
     * It is recommended to set additional conditions to filter out as many items as possible.
     *
     * <p>Example:
     * <pre>{@code
     *  ClassFilter.builder()
     *      .setReferenceTypes(ReferenceTypes.STRINGS_ONLY)
     *      .setReferenceFilter(pool ->
     *          pool.contains("a string inside the desired class")
     *      )
     *      .setModifiers(Modifier.PUBLIC)  // Additional: search in public classes only
     *      .build()
     *  // This matches classes only if they contain the given string in their reference pool.
     *  ...
     * }</pre>
     *
     * @param filter the filter to apply to the {@code ReferencePool} of each item
     * @return {@code this} builder
     * @throws IllegalStateException if reference {@link #setReferenceTypes(ReferenceTypes) types} were not specified
     * @see #setReferenceTypes(ReferenceTypes)
     */
    public B setReferenceFilter(@Nullable ReferenceFilter filter) {
      if (this.types == null) {
        throw new IllegalStateException("Reference types were not specified");
      }
      this.filter = filter;
      return getThis();
    }
  }
}
