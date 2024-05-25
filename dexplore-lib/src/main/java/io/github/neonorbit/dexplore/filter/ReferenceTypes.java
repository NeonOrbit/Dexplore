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

import io.github.neonorbit.dexplore.iface.Internal;
import io.github.neonorbit.dexplore.reference.FieldRefData;
import io.github.neonorbit.dexplore.reference.MethodRefData;
import io.github.neonorbit.dexplore.reference.StringRefData;
import io.github.neonorbit.dexplore.reference.TypeRefData;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Represents a set of reference types.
 * <p>
 * Instances of this class may be passed to the
 * {@link BaseFilter.Builder#setReferenceTypes(ReferenceTypes)
 *        setReferenceTypes()}
 * method.
 * <p>
 * Use the {@link Builder Builder} class to create ReferenceTypes instances.
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public final class ReferenceTypes {
  private static final int NONE   = 0x0000;
  private static final int STRING = 0x0001;
  private static final int TYPED  = 0x0002;
  private static final int FIELD  = 0x0004;
  private static final int METHOD = 0x0008;
  private static final int F_INFO = 0x0010;
  private static final int M_INFO = 0x0020;

  private static final int ALL = STRING | TYPED  | FIELD | METHOD | F_INFO | M_INFO;

  /**
   * An instance that includes all reference types.
   * <p>Note: Use {@link Builder Builder} for finer control.</p>
   */
  public static final ReferenceTypes ALL_TYPES = builder().addAll().build();

  /**
   * An instance that includes the {@link StringRefData String} reference type only.
   * <p>Note: Use {@link Builder Builder} for finer control.</p>
   */
  public static final ReferenceTypes STRINGS_ONLY = builder().addString().build();

  /**
   * Represents the scope of references.
   * <p>
   * See Also: {@link Builder#setScope(Scope) setScope(Scope)}
   * <p>
   * {@link #ALL}: All methods <br>
   * {@link #DIRECT}: Direct methods only (any of static, private, or constructor) <br>
   * {@link #VIRTUAL}: Virtual methods only (none of static, private, or constructor)
   */
  public enum Scope {
    /** All methods */
    ALL,
    /** Do Not Use */
    NONE,
    /** Direct methods only (any of static, private, or constructor) */
    DIRECT,
    /** Virtual methods only (none of static, private, or constructor) */
    VIRTUAL
  }

  private int hash;
  private final int flags;
  private final Scope scope;
  private final boolean synthetic;

  private ReferenceTypes(Builder builder) {
    this.flags = builder.flags;
    this.scope = builder.scope;
    this.synthetic = builder.synthetic;
  }

  @Internal
  ReferenceTypes withSynthetic(boolean enable) {
    if (synthetic == enable) return this;
    return toBuilder().enableSynthetic(enable).build();
  }

  public Scope getScope() {
    return scope;
  }

  public boolean synthRefs() {
    return synthetic;
  }

  public boolean hasAll() {
    return (ALL & flags) == ALL;
  }

  public boolean hasNone() {
    return (NONE | flags) == NONE;
  }

  public boolean hasString() {
    return (STRING & flags) != NONE;
  }

  public boolean hasTypeDes() {
    return (TYPED & flags) != NONE;
  }

  public boolean hasField() {
    return (FIELD & flags) != NONE;
  }

  public boolean hasMethod() {
    return (METHOD & flags) != NONE;
  }

  public boolean hasFieldDetails() {
    return (F_INFO & flags) != NONE;
  }

  public boolean hasMethodDetails() {
    return (M_INFO & flags) != NONE;
  }

  @Override
  public int hashCode() {
    if (hash == 0) {
      int h = 1;
      h = 31 * h + flags;
      h = 31 * h + scope.hashCode();
      h = 31 * h + ((Boolean) synthetic).hashCode();
      this.hash = h;
    }
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj instanceof ReferenceTypes) {
      ReferenceTypes another = (ReferenceTypes) obj;
      return this.flags == another.flags &&
             this.scope == another.scope &&
             this.synthetic == another.synthetic;
    }
    return false;
  }

  /**
   * @return an instance that includes all reference types.
   */
  public static ReferenceTypes all() {
    return builder().addAll().build();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder for creating {@code ReferenceTypes} instances.
   * <p>Example:</p>
   * <pre>{@code
   *    ReferenceTypes.builder().addString().addMethod().build()
   * }</pre>
   */
  public static class Builder {
    private int flags = NONE;
    private boolean synthetic;
    private Scope scope = Scope.ALL;

    private Builder() {}

    private Builder(ReferenceTypes instance) {
      this.flags = instance.flags;
      this.scope = instance.scope;
      this.synthetic = instance.synthetic;
    }

    public ReferenceTypes build() {
      return new ReferenceTypes(this);
    }

    private Builder enableSynthetic(boolean enable) {
      this.synthetic = enable;
      return this;
    }

    /**
     * Add all reference types.
     * @return {@code this} builder
     */
    public Builder addAll() {
      this.flags = ALL;
      return this;
    }

    /**
     * Enables {@link StringRefData String} reference type.
     * @return {@code this} builder
     */
    public Builder addString() {
      this.flags |= STRING;
      return this;
    }

    /**
     * Enables {@link TypeRefData Type} reference type.
     * @return {@code this} builder
     */
    public Builder addTypeDes() {
      this.flags |= TYPED;
      return this;
    }

    /**
     * Enables {@link FieldRefData Field} reference type.
     * <p>
     * <b>Note:</b> This includes only the names of fields.
     * See {@link #addFieldWithDetails()}
     *
     * @return {@code this} builder
     */
    public Builder addField() {
      this.flags |= FIELD;
      return this;
    }

    /**
     * Enables {@link MethodRefData Method} reference type.
     * <p>
     * <b>Note:</b> This includes only the names of methods.
     * See {@link #addMethodWithDetails()}
     *
     * @return {@code this} builder
     */
    public Builder addMethod() {
      this.flags |= METHOD;
      return this;
    }

    /**
     * Enables {@link FieldRefData Field} reference type.
     * <p>
     * <b>Note:</b> This includes the full
     *    details ({@linkplain FieldRefData#getName() name},
     *             {@linkplain FieldRefData#getType() type},
     *             {@linkplain FieldRefData#getDeclaringClass() class})
     *    of fields.
     * <p>
     * <b>API Note:</b> This is slower than {@link #addField()}
     *
     * @return {@code this} builder
     */
    public Builder addFieldWithDetails() {
      this.flags |= FIELD | F_INFO;
      return this;
    }

    /**
     * Enables {@link MethodRefData Method} reference type.
     * <p>
     * <b>Note:</b> This includes the full
     *    details ({@linkplain MethodRefData#getName() name},
     *             {@linkplain MethodRefData#getParameterTypes() params},
     *             {@linkplain MethodRefData#getReturnType() return-type},
     *             {@linkplain MethodRefData#getDeclaringClass() class})
     *    of methods.
     * <p>
     * <b>API Note:</b> This is slower than {@link #addMethod()}
     *
     * @return {@code this} builder
     */
    public Builder addMethodWithDetails() {
      this.flags |= METHOD | M_INFO;
      return this;
    }

    /**
     * Specifies the {@linkplain Scope scope}, from which the references should be added.
     * <p>
     * <b>Default: </b> {@link Scope#ALL}
     *
     * @param scope the scope
     * @return {@code this} builder
     */
    public Builder setScope(@Nonnull Scope scope) {
      this.scope = Objects.requireNonNull(scope);
      return this;
    }
  }
}
