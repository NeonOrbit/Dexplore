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

import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;

import javax.annotation.Nonnull;

public final class StringReferenceData implements DexReferenceData {
  private boolean resolved;
  private StringReference data;

  private StringReferenceData(StringReference reference) {
    this.data = reference;
  }

  public static StringReferenceData build(StringReference reference) {
    return new StringReferenceData(reference);
  }

  public static StringReferenceData build(String value) {
    StringReferenceData data = build(new ImmutableStringReference(value));
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

  @Nonnull
  public String getString() {
    return getData().getString();
  }

  /**
   * Checks whether the value of this {@code String} matches the specified string
   *
   * @param value The string to compare against
   * @return {@code true} if this {@code Object} contains the specified string
   */
  @Override
  public boolean contains(@Nonnull String value) {
    return getData().getString().equals(value);
  }

  @Override
  public int hashCode() {
    return getData().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return (this == obj) || (obj instanceof StringReferenceData) &&
            (this.getData().equals(((StringReferenceData) obj).getData()));
  }

  @Override
  public String toString() {
    return getData().getString();
  }
}
