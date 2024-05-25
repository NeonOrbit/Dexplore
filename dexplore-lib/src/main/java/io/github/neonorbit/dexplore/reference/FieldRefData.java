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
import io.github.neonorbit.dexplore.result.FieldData;
import io.github.neonorbit.dexplore.util.DexUtils;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.immutable.reference.ImmutableFieldReference;

import javax.annotation.Nonnull;

/**
 * This class represents a {@linkplain io.github.neonorbit.dexplore.reference reference} to a field identifier.
 * <p>
 * Constant values:
 * <ul>
 *   <li>{@link #getDeclaringClass() class} - the declaring class of the field.</li>
 *   <li>{@link #getName() name} - the name of the field.</li>
 *   <li>{@link #getType() type} - the type of the field.</li>
 * </ul>
 *
 * @see ReferencePool
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public final class FieldRefData implements DexRefData {
  private final boolean details;
  private boolean resolved;
  private FieldReference data;

  private FieldRefData(FieldReference reference, boolean details) {
    this.details = details;
    this.data = reference;
  }

  public static FieldRefData build(FieldReference reference, boolean details) {
    return new FieldRefData(reference, details);
  }

  private FieldReference getData() {
    if (!resolved) {
      resolved = true;
      data = new ImmutableFieldReference(
              details ? DexUtils.dexToJavaTypeName(data.getDefiningClass()) : "",
              data.getName(),
              details ? DexUtils.dexToJavaTypeName(data.getType()) : ""
      );
    }
    return data;
  }

  /**
   * @return the field name
   */
  @Nonnull
  public String getName() {
    return getData().getName();
  }

  /**
   * @return {@linkplain Class#getName() full name} of the field type
   */
  @Nonnull
  public String getType() {
    return getData().getType();
  }

  /**
   * @return {@linkplain Class#getName() full name} of the declaring class of the field
   */
  @Nonnull
  public String getDeclaringClass() {
    return getData().getDefiningClass();
  }

  /**
   * Checks if the reference contains the specified value.
   * <p>
   * More precisely, it returns {@code true} if at least one
   * {@link FieldRefData constant} value of the reference is {@code equal} to the specified value.
   */
  @Override
  public boolean contains(@Nonnull String value) {
    return getName().equals(value) || details && (
            getType().equals(value) || getDeclaringClass().equals(value)
    );
  }

  /**
   * Returns the signature of the field.
   * <p>
   * Format: class.<b>fieldName</b>:fieldType <br>
   * Example: java.lang.Byte.<b><u>{@linkplain Byte#BYTES BYTES}</u></b>:int
   * <p>
   * <b>Note:</b> A new signature string is generated with each method invocation.
   * @return field signature
   */
  @Nonnull
  @Override
  public String getSignature() {
    return !details ? DexUtils.getFieldSignature(getName()) :
            DexUtils.getFieldSignature(getDeclaringClass(), getName(), getType());
  }

  /**
   * @return a {@link FieldData} object representing the field
   */
  @Nonnull
  public FieldData toFieldData() {
    return FieldData.of(this);
  }

  @Override
  public int hashCode() {
    return getData().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return (this == obj) || (obj instanceof FieldRefData) && (
            this.getData().equals(((FieldRefData) obj).getData())
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
