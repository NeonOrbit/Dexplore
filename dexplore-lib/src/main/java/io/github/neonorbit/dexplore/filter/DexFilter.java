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

import io.github.neonorbit.dexplore.AbortException;
import io.github.neonorbit.dexplore.DexEntry;
import io.github.neonorbit.dexplore.LazyDecoder;
import io.github.neonorbit.dexplore.util.DexUtils;
import io.github.neonorbit.dexplore.util.Utils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class DexFilter extends BaseFilter<DexEntry> {
  /** A {@code DexFilter} instance that matches all dex files. */
  public static final DexFilter MATCH_ALL = new DexFilter(builder());

  private final boolean preferredDexOnly;
  private final Set<String> definedClassNames;
  private final List<String> preferredDexNames;

  private DexFilter(Builder builder) {
    super(builder, Utils.isSingle(builder.definedClassNames));
    this.preferredDexOnly = builder.preferredDexOnly;
    this.preferredDexNames = builder.preferredDexNames;
    this.definedClassNames = builder.definedClassNames;
  }

  public List<String> preferredList() {
    return preferredDexNames;
  }

  @Override
  public boolean verify(@Nonnull DexEntry dexEntry,
                        @Nonnull LazyDecoder<DexEntry> decoder) {
    if (this == MATCH_ALL) return true;
    if (shouldTerminate(dexEntry)) {
      throw AbortException.silently();
    }
    if (definedClassNames != null &&
        dexEntry.getDexFile().getClasses().stream()
                .noneMatch(c -> definedClassNames.contains(c.getType()))) {
      return false;
    }
    boolean result = super.verify(dexEntry, decoder);
    if (unique && !result) {
      throw new AbortException("Dex found but the filter didn't match");
    }
    return result;
  }

  private boolean shouldTerminate(DexEntry dexEntry) {
    if (preferredDexOnly && preferredDexNames != null) {
      return !preferredDexNames.contains(dexEntry.getDexName());
    }
    return false;
  }

  public static DexFilter ofDefinedClass(@Nonnull String clazz) {
    Objects.requireNonNull(clazz);
    return builder().setDefinedClasses(clazz).build();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder extends BaseFilter.Builder<Builder, DexFilter> {
    private boolean preferredDexOnly;
    private Set<String> definedClassNames;
    private List<String> preferredDexNames;

    public Builder() {}

    private Builder(DexFilter instance) {
      super(instance);
      this.preferredDexOnly = instance.preferredDexOnly;
      this.preferredDexNames = instance.preferredDexNames;
      this.definedClassNames = instance.definedClassNames;
    }

    @Override
    protected boolean isDefault() {
      return super.isDefault() &&
             !preferredDexOnly &&
             definedClassNames == null &&
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

    public Builder setPreferredDexNames(@Nonnull String... names) {
      List<String> list = Utils.nonNullList(names);
      this.preferredDexNames = list.isEmpty() ? null : Utils.optimizedList(list);
      return this;
    }

    public Builder allowPreferredDexOnly(boolean allow) {
      if (preferredDexNames == null) {
        throw new IllegalStateException("Preferred dex was not specified");
      }
      this.preferredDexOnly = allow;
      return this;
    }

    public Builder setDefinedClasses(@Nonnull String... classes) {
      List<String> list = DexUtils.javaToDexTypeName(Utils.nonNullList(classes));
      this.definedClassNames = list.isEmpty() ? null : Utils.optimizedSet(list);
      return this;
    }
  }
}
