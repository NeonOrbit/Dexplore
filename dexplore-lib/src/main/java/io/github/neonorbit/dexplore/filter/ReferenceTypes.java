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

import javax.annotation.Nonnull;
import java.util.Objects;

public final class ReferenceTypes {
  private static final int NONE   = 0x0000;
  private static final int STRING = 0x0001;
  private static final int TYPED  = 0x0002;
  private static final int FIELD  = 0x0004;
  private static final int METHOD = 0x0008;
  private static final int F_INFO = 0x0010;
  private static final int M_INFO = 0x0020;

  private static final int ALL = STRING | TYPED  | FIELD |
                                 METHOD | F_INFO | M_INFO;

  /**
   * {@code ALL}: Include all methods <br>
   * {@code NONE}: Skip all methods <br>
   * {@code DIRECT}: Include direct methods only
   * <pre>  (any of static, private, or constructor) </pre>
   * {@code VIRTUAL}: Include virtual methods only
   * <pre>  (none of static, private, or constructor) </pre>
   */
  public enum Scope {
    ALL, NONE, DIRECT, VIRTUAL
  }

  private int hash;
  private final int flags;
  private final Scope scope;

  private ReferenceTypes(Builder builder) {
    this.flags = builder.flags;
    this.scope = builder.scope;
  }

  public Scope getScope() {
    return scope;
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
      h = 31 * h + scope.ordinal();
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
             this.scope.ordinal() == another.scope.ordinal();
    }
    return false;
  }

  @Override
  public String toString() {
    return "S" + scope.ordinal() + "F" + flags;
  }

  public static ReferenceTypes all() {
    return builder().addAll().build();
  }

  public static ReferenceTypes none() {
    return builder().setScope(Scope.NONE).build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private int flags = NONE;
    private Scope scope = Scope.ALL;

    public ReferenceTypes build() {
      return new ReferenceTypes(this);
    }

    public Builder addAll() {
      this.flags = ALL;
      return this;
    }

    public Builder addString() {
      this.flags |= STRING;
      return this;
    }

    public Builder addTypeDes() {
      this.flags |= TYPED;
      return this;
    }

    public Builder addField() {
      this.flags |= FIELD;
      return this;
    }

    public Builder addMethod() {
      this.flags |= METHOD;
      return this;
    }

    /**
     * Include all details: name, class, type
     *
     * @return this builder
     */
    public Builder addFieldWithDetails() {
      this.flags |= FIELD | F_INFO;
      return this;
    }

    /**
     * Include full signature: name, class, params, return type
     *
     * @return this builder
     */
    public Builder addMethodWithDetails() {
      this.flags |= METHOD | M_INFO;
      return this;
    }

    public Builder setScope(@Nonnull Scope scope) {
      this.scope = Objects.requireNonNull(scope);
      return this;
    }
  }
}
