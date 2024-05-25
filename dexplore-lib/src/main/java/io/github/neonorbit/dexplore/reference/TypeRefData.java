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
import io.github.neonorbit.dexplore.result.ClassData;
import io.github.neonorbit.dexplore.util.DexUtils;
import org.jf.dexlib2.iface.reference.TypeReference;
import org.jf.dexlib2.immutable.reference.ImmutableTypeReference;

import javax.annotation.Nonnull;

/**
 * This class represents a {@linkplain io.github.neonorbit.dexplore.reference reference} to a type.
 * <p>
 * Constant value:
 * <ul>
 *   <li>{@link #getType() type} - type name (typically the {@linkplain Class#getName() full name} of a class).</li>
 * </ul>
 *
 * @see ReferencePool
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public final class TypeRefData implements DexRefData {
  private boolean resolved;
  private TypeReference data;

  private TypeRefData(TypeReference reference) {
    this.data = reference;
  }

  public static TypeRefData build(TypeReference reference) {
    return new TypeRefData(reference);
  }

  private TypeReference getData() {
    if (!resolved) {
      resolved = true;
      data = new ImmutableTypeReference(DexUtils.dexToJavaTypeName(data.getType()));
    }
    return data;
  }

  /**
   * @return {@linkplain Class#getName() full name} of the type
   */
  @Nonnull
  public String getType() {
    return getData().getType();
  }

  /**
   * Checks if the reference contains the specified value.
   * <p>
   * More precisely, it returns {@code true} if the
   * {@link #getType() type name} is {@code equal} to the specified value.
   */
  @Override
  public boolean contains(@Nonnull String value) {
    return getType().equals(value);
  }

  /**
   * Returns the signature of the type.
   * <p>
   * Format: FullClassName <br>
   * Example: java.lang.Byte
   * <p>
   * <b>Note:</b> This method is similar to {@linkplain #getType()}
   * and is included for compatibility with other reference types.
   * @return type signature
   */
  @Nonnull
  @Override
  public String getSignature() {
    return getType();
  }

  /**
   * @return a {@code ClassData} object representing the type (class)
   */
  @Nonnull
  public ClassData toClassData() {
    return ClassData.of(this);
  }

  @Override
  public int hashCode() {
    return getData().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return (this == obj) || (obj instanceof TypeRefData) && (
            this.getData().equals(((TypeRefData) obj).getData())
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
