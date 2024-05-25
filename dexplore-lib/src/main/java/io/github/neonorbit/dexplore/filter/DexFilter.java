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

import io.github.neonorbit.dexplore.DexEntry;
import io.github.neonorbit.dexplore.LazyDecoder;
import io.github.neonorbit.dexplore.exception.AbortException;
import io.github.neonorbit.dexplore.iface.Internal;
import io.github.neonorbit.dexplore.util.Utils;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * A filter that determines the dex files to be loaded for searching.
 * <p>
 * <b>Note:</b> The filter matches only if all the specified conditions are satisfied.
 * <p>
 * Use the {@link Builder Builder} class to create filter instances.
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public final class DexFilter extends BaseFilter<DexEntry> {
  /** A {@code DexFilter} instance that matches all dex files. */
  public static final DexFilter MATCH_ALL = new DexFilter(builder());

  private final boolean preferredDexOnly;
  private final List<String> preferredDexNames;

  private DexFilter(Builder builder) {
    super(builder, false);
    this.preferredDexOnly = builder.preferredDexOnly;
    this.preferredDexNames = builder.preferredDexNames;
  }

  @Internal
  public List<String> preferredList() {
    return preferredDexNames;
  }

  @Internal
  @Override
  public boolean verify(@Nonnull DexEntry dexEntry,
                        @Nonnull LazyDecoder<DexEntry> decoder) {
    if (this == MATCH_ALL) return true;
    if (shouldTerminate(dexEntry)) {
      throw AbortException.silently();
    }
    return super.verify(dexEntry, decoder);
  }

  private boolean shouldTerminate(DexEntry dexEntry) {
    if (preferredDexOnly && preferredDexNames != null) {
      return !preferredDexNames.contains(dexEntry.getDexName());
    }
    return false;
  }

  /**
   * Currently does nothing.
   * @deprecated {@link
   *    ClassFilter.Builder#setClasses(String...)
   *    ClassFilter.setClasses()
   * } makes it obsolete
   * @param clazz class name
   * @return a {@code DexFilter} instance
   */
  @Deprecated
  @SuppressWarnings("unused")
  public static DexFilter ofDefinedClass(@Nonnull String clazz) {
    return MATCH_ALL;
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder for creating {@code DexFilter} instances.
   * <p>
   * <b>Note:</b> The filter matches only if all the specified conditions are satisfied.
   * <p>Example:
   * <pre>{@code
   *  DexFilter.builder()
   *      .setPreferredDexNames(...)
   *      .......
   *      .build()
   *  ...
   * }</pre>
   */
  public static class Builder extends BaseFilter.Builder<Builder, DexFilter> {
    private boolean preferredDexOnly;
    private List<String> preferredDexNames;

    public Builder() {}

    private Builder(DexFilter instance) {
      super(instance);
      this.preferredDexOnly = instance.preferredDexOnly;
      this.preferredDexNames = instance.preferredDexNames;
    }

    @Override
    protected boolean isDefault() {
      return super.isDefault()  &&
              !preferredDexOnly &&
              preferredDexNames == null;
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    @Override
    public DexFilter build() {
      return isDefault() ? MATCH_ALL : new DexFilter(this);
    }

    /**
     * Specify a prioritized list of dex files to be searched first.
     *
     * @param names dex file names
     * @return {@code this} builder
     */
    public Builder setPreferredDexNames(@Nonnull String... names) {
      List<String> list = Utils.nonNullList(names);
      this.preferredDexNames = list.isEmpty() ? null : Utils.optimizedList(list);
      return this;
    }

    /**
     * If true, only the {@link #setPreferredDexNames(String...) preferred} dex files will be searched.
     *
     * @param allow {@code true} to set, {@code false} to unset.
     * @return {@code this} builder
     * @throws IllegalStateException if preferred dex was not specified
     */
    public Builder allowPreferredDexOnly(boolean allow) {
      if (allow && preferredDexNames == null) {
        throw new IllegalStateException("Preferred dex was not specified");
      }
      this.preferredDexOnly = allow;
      return this;
    }

    /**
     * Currently does nothing.
     * @deprecated {@link
     *    ClassFilter.Builder#setSourceNames(String...)
     *    ClassFilter.setSourceNames()
     * } makes it obsolete
     * @param sources source file names
     * @return {@code this} builder
     */
    @Deprecated
    @SuppressWarnings("all")
    public Builder setStoredSources(@Nonnull String... sources) {
      return this;
    }

    /**
     * Currently does nothing.
     * @deprecated {@link
     *    ClassFilter.Builder#setClasses(String...)
     *    ClassFilter.setClasses()
     * } makes it obsolete
     * @param classes full names of classes
     * @return {@code this} builder
     */
    @Deprecated
    @SuppressWarnings("all")
    public Builder setDefinedClasses(@Nonnull String... classes) {
      return this;
    }
  }
}
