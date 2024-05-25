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

package io.github.neonorbit.dexplore.reference;

import io.github.neonorbit.dexplore.ReferencePool;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;

import javax.annotation.Nonnull;

/**
 * This class represents a {@linkplain io.github.neonorbit.dexplore.reference reference} to a string literal.
 * <p>
 * Constant value:
 * <ul>
 *   <li>{@link #getString() string} - the literal string.</li>
 * </ul>
 * @see ReferencePool
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public final class StringRefData implements DexRefData {
  private boolean resolved;
  private StringReference data;

  private StringRefData(StringReference reference) {
    this.data = reference;
  }

  public static StringRefData build(StringReference reference) {
    return new StringRefData(reference);
  }

  public static StringRefData build(String value) {
    StringRefData data = build(new ImmutableStringReference(value));
    data.resolved = true;
    return data;
  }

  private StringReference getData() {
    if (!resolved) {
      resolved = true;
      data = ImmutableStringReference.of(data);
    }
    return data;
  }

  /**
   * @return the literal string
   */
  @Nonnull
  public String getString() {
    return getData().getString();
  }

  /**
   * Checks if the reference contains the specified value.
   * <p>
   * More precisely, it returns {@code true} if the literal
   * {@link #getString() string} is {@code equal} to the specified value.
   */
  @Override
  public boolean contains(@Nonnull String value) {
    return getString().equals(value);
  }

  /**
   * Returns the signature of the string reference.
   * <p>
   * <b>Note:</b> This method is similar to {@linkplain #getString()}
   * and is included for compatibility with other reference types.
   */
  @Nonnull
  @Override
  public String getSignature() {
    return getString();
  }

  @Override
  public int hashCode() {
    return getData().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return (this == obj) || (obj instanceof StringRefData) && (
            this.getData().equals(((StringRefData) obj).getData())
    );
  }

  /**
   * Equivalent to {@link #getSignature()}
   */
  @Override
  public String toString() {
    return getSignature();
  }
}
