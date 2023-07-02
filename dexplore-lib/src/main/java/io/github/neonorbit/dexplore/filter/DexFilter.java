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
import io.github.neonorbit.dexplore.util.DexUtils;
import io.github.neonorbit.dexplore.util.Utils;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static io.github.neonorbit.dexplore.util.Utils.isSingle;

/**
 * A filter that decides whether a dex file should be loaded for analyzing.
 * <p><br>
 *   Note: The filter will match if and only if all the specified conditions are satisfied.
 * </p>
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public final class DexFilter extends BaseFilter<DexEntry> {
  /** A {@code DexFilter} instance that matches all dex files. */
  public static final DexFilter MATCH_ALL = new DexFilter(builder());

  private final boolean preferredDexOnly;
  private final Set<String> storedSourceNames;
  private final Set<String> definedClassNames;
  private final List<String> preferredDexNames;

  private DexFilter(Builder builder) {
    super(builder, isSingle(builder.definedClassNames));
    this.preferredDexOnly = builder.preferredDexOnly;
    this.preferredDexNames = builder.preferredDexNames;
    this.storedSourceNames = builder.storedSourceNames;
    this.definedClassNames = builder.definedClassNames;
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
    if (!checkDefinedClasses(dexEntry)) return false;
    boolean result = (
            checkStoredSources(dexEntry) &&
            super.verify(dexEntry, decoder)
    );
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

  private boolean checkStoredSources(DexEntry dexEntry) {
    if (storedSourceNames == null) return true;
    for (DexBackedClassDef c : DexUtils.dexClasses(dexEntry.getDexFile())) {
      String source = c.getSourceFile();
      if (source != null && storedSourceNames.contains(source)) {
        return true;
      }
    }
    return false;
  }

  private boolean checkDefinedClasses(DexEntry dexEntry) {
    if (definedClassNames == null) return true;
    for (DexBackedClassDef c : DexUtils.dexClasses(dexEntry.getDexFile())) {
      if (definedClassNames.contains(c.getType())) return true;
    }
    return false;
  }

  /**
   * This is equivalent to:
   * <blockquote><pre>
   *   new DexFilter.Builder()
   *                .{@link DexFilter.Builder#setDefinedClasses(String...)
   *                             setDefinedClasses(clazz)}
   *                .build();
   * </pre></blockquote>
   *
   * @param clazz class name
   * @return a {@code DexFilter} instance
   */
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
    private Set<String> storedSourceNames;
    private Set<String> definedClassNames;
    private List<String> preferredDexNames;

    public Builder() {}

    private Builder(DexFilter instance) {
      super(instance);
      this.preferredDexOnly = instance.preferredDexOnly;
      this.preferredDexNames = instance.preferredDexNames;
      this.storedSourceNames = instance.storedSourceNames;
      this.definedClassNames = instance.definedClassNames;
    }

    @Override
    protected boolean isDefault() {
      return super.isDefault()  &&
              !preferredDexOnly &&
              preferredDexNames == null &&
              storedSourceNames == null &&
              definedClassNames == null;
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
     * Specify a list of dex files that should be analyzed first.
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
     * If true, only the {@link #setPreferredDexNames(String...) preferred} dex files will be analyzed.
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
     * Add a condition to the filter to match dex files that contain any of the specified source files.
     * <p>Examples: "Application.java", "AnyFileName.java" etc.</p>
     *
     * @param sources source file names
     * @return {@code this} builder
     */
    public Builder setStoredSources(@Nonnull String... sources) {
      List<String> list = Utils.nonNullList(sources);
      this.storedSourceNames = list.isEmpty() ? null : Utils.optimizedSet(list);
      return this;
    }

    /**
     * Add a condition to the filter to match dex files that contain any of the specified classes.
     * <p>This is useful if you want to search in specific classes only.</p>
     *
     * @param classes {@linkplain Class#getName() full names} of classes
     * @return {@code this} builder
     */
    public Builder setDefinedClasses(@Nonnull String... classes) {
      List<String> list = DexUtils.javaToDexTypeName(Utils.nonNullList(classes));
      this.definedClassNames = list.isEmpty() ? null : Utils.optimizedSet(list);
      return this;
    }
  }
}
