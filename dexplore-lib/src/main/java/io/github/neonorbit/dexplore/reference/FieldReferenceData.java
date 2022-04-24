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
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.immutable.reference.ImmutableFieldReference;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public final class FieldReferenceData implements DexReferenceData {
  private final boolean details;
  private boolean resolved;
  private String signature;
  private FieldReference data;

  private FieldReferenceData(FieldReference reference, boolean details) {
    this.details = details;
    this.data = reference;
  }

  public static FieldReferenceData build(FieldReference reference, boolean details) {
    return new FieldReferenceData(reference, details);
  }

  private FieldReference getData() {
    if (!resolved) {
      resolved = true;
      String name = data.getName();
      String from = details ? DexUtils.dexToJavaTypeName(data.getDefiningClass()) : "";
      String type = details ? DexUtils.dexToJavaTypeName(data.getType()) : "";
      data = new ImmutableFieldReference(from, name, type);
    }
    return data;
  }

  /**
   * Equivalent to {@link Field#getName()}
   *
   * @return field name
   */
  @Nonnull
  public String getName() {
    return getData().getName();
  }

  /**
   * Equivalent to {@link Field#getType()}
   *
   * @return field type
   */
  @Nonnull
  public String getType() {
    return getData().getType();
  }

  /**
   * Equivalent to {@link Field#getDeclaringClass()}
   *
   * @return declaring class
   */
  @Nonnull
  public String getDeclaringClass() {
    return getData().getDefiningClass();
  }

  /**
   * Checks whether any items of this {@code Field} match the specified string
   *
   * @param value The string to compare against
   * @return {@code true} if this {@code Object} contains the specified string
   */
  @Override
  public boolean contains(@Nonnull String value) {
    final FieldReference ref = getData();
    return ref.getName().equals(value) ||
           details && (
              ref.getType().equals(value) ||
              ref.getDefiningClass().equals(value)
           );
  }

  /**
   * Signature: className.<b>fieldName</b>:fieldType
   * <p>Example: java.lang.Byte.<b>SIZE</b>:int</p>
   *
   * @return field signature
   */
  public String getSignature() {
    if (signature == null) {
      FieldReference ref = getData();
      String name = ref.getName();
      String from = details ? ref.getDefiningClass() : "[blank]";
      String type = details ? ref.getType() : "[blank]";
      signature = DexUtils.getFieldSignature(from, name, type);
    }
    return signature;
  }

  @Override
  public int hashCode() {
    return getData().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return (this == obj) || (obj instanceof FieldReferenceData) &&
           (this.getData().equals(((FieldReferenceData)obj).getData()));
  }

  /**
   * Equivalent to {@link #getSignature()}
   */
  @Override
  public String toString() {
    return getSignature();
  }
}
