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

import io.github.neonorbit.dexplore.util.DexUtils;
import org.jf.dexlib2.iface.reference.TypeReference;
import org.jf.dexlib2.immutable.reference.ImmutableTypeReference;

import javax.annotation.Nonnull;

public final class TypeReferenceData implements DexReferenceData {
  private boolean resolved;
  private TypeReference data;

  private TypeReferenceData(TypeReference reference) {
    this.data = reference;
  }

  public static TypeReferenceData build(TypeReference reference) {
    return new TypeReferenceData(reference);
  }

  private TypeReference getData() {
    if (!resolved) {
      resolved = true;
      data = new ImmutableTypeReference(
                   DexUtils.dexToJavaTypeName(data.getType())
                 );
    }
    return data;
  }

  @Nonnull
  public String getType() {
    return getData().getType();
  }

  /**
   * Checks whether the value of this {@code Type} matches the specified string
   *
   * @param value The string to compare against
   * @return {@code true} if this {@code Object} contains the specified string
   */
  @Override
  public boolean contains(@Nonnull String value) {
    return getData().getType().equals(value);
  }

  @Override
  public int hashCode() {
    return getData().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return (this == obj) || (obj instanceof TypeReferenceData) &&
           (this.getData().equals(((TypeReferenceData)obj).getData()));
  }

  @Override
  public String toString() {
    return getData().getType();
  }
}
