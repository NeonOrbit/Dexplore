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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class DexFilter extends BaseFilter<DexEntry> {
  public  final String preferredDexName;
  private final boolean preferredDexOnly;
  private final boolean preferredDexPass;
  private final Set<String> definedClassNames;

  private final String definedClassName;

  private DexFilter(Builder builder) {
    super(builder);
    this.preferredDexName = builder.preferredDexName;
    this.preferredDexOnly = builder.preferredDexOnly;
    this.preferredDexPass = builder.preferredDexPass;
    this.definedClassNames = builder.definedClassNames;
    if (this.definedClassNames == null ||
        this.definedClassNames.size() != 1) {
      this.definedClassName = null;
    } else {
      this.definedClassName = this.definedClassNames.iterator().next();
    }
  }

  @Override
  public boolean verify(@Nonnull DexEntry dexEntry,
                        @Nonnull LazyDecoder<DexEntry> decoder) {
    boolean isPreferred = dexEntry.getDexName().equals(preferredDexName);
    if (preferredDexOnly && !isPreferred) {
      throw new AbortException();
    }
    if (preferredDexPass && isPreferred) {
      return true;
    } else if (definedClassName != null) {
      return dexEntry.getDexFile().getClasses().stream()
                     .anyMatch(c -> definedClassName.equals(c.getType()));
    } else if (definedClassNames != null) {
      return dexEntry.getDexFile().getClasses().stream()
                     .anyMatch(c -> definedClassNames.contains(c.getType()));
    } else {
      return super.verify(dexEntry, decoder);
    }
  }

  public static DexFilter ofDefinedClass(@Nonnull String clazz) {
    Objects.requireNonNull(clazz);
    return builder().setDefinedClasses(clazz).build();
  }

  public static DexFilter none() {
    return builder().build();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder extends BaseFilter.Builder<Builder, DexFilter> {
    public String preferredDexName;
    public boolean preferredDexOnly;
    public boolean preferredDexPass;
    private Set<String> definedClassNames;

    public Builder() {}

    private Builder(DexFilter instance) {
      super(instance);
      this.preferredDexName = instance.preferredDexName;
      this.preferredDexOnly = instance.preferredDexOnly;
      this.preferredDexPass = instance.preferredDexPass;
      this.definedClassNames = instance.definedClassNames;
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    @Override
    public DexFilter build() {
      return new DexFilter(this);
    }

    public Builder setPreferredDex(@Nullable String dexName) {
      this.preferredDexName = dexName;
      return this;
    }

    public Builder allowPreferredDexOnly(boolean prefDexOnly) {
      if (preferredDexName == null) {
        throw new IllegalArgumentException("Preferred Dex was not defined");
      }
      this.preferredDexOnly = prefDexOnly;
      return this;
    }

    public Builder skipPreferredDexCheck(boolean skipPrefDexCheck) {
      if (preferredDexName == null) {
        throw new IllegalArgumentException("Preferred Dex was not defined");
      }
      this.preferredDexPass = skipPrefDexCheck;
      return this;
    }

    public Builder setDefinedClasses(@Nullable String... classes) {
      this.definedClassNames = classes == null || classes.length == 0 ? null :
                               new HashSet<>(DexUtils.javaToDexTypeName(Arrays.asList(classes)));
      return this;
    }
  }
}
