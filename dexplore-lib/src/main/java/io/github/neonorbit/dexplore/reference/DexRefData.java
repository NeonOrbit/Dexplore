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

import javax.annotation.Nonnull;

/**
 * Represents a dex {@linkplain io.github.neonorbit.dexplore.reference reference}.
 *
 * @see StringRefData
 * @see TypeRefData
 * @see FieldRefData
 * @see MethodRefData
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public interface DexRefData {
  /**
   * Returns the signature of the reference.
   * <p>
   * Signature Formats:
   * <ul>
   *   <li>
   *     {@linkplain StringRefData}: <br>
   *     Format: string-literal <br>
   *     Example: "StringLiteral" <br>
   *     .
   *   </li>
   *   <li>
   *     {@linkplain TypeRefData}: <br>
   *     Format: FullClassName <br>
   *     Example: java.lang.Byte <br>
   *     .
   *   </li>
   *   <li>
   *     {@linkplain FieldRefData}: <br>
   *     Format: class.<b>fieldName</b>:fieldType <br>
   *     Example: java.lang.Byte.<b><u>{@linkplain Byte#BYTES BYTES}</u></b>:int <br>
   *     .
   *   </li>
   *   <li>
   *     {@linkplain MethodRefData}: <br>
   *     Format: class.<b>method</b>(param1,param2,paramN):returnType <br>
   *     Example: java.lang.Byte.<b><u>{@linkplain Byte#parseByte parseByte}</u></b>(java.lang.String,int):byte
   *   </li>
   * </ul>
   *
   * @return reference signature
   */
  @Nonnull
  String getSignature();

  /**
   * Checks if the reference contains the specified value.
   * <p>
   * <b>Note:</b> A dex reference can consist of one or more constant values.
   * This method returns true if at least one constant within the reference {@code equals} the specified value.
   * <p>
   * Constants in References:
   * <ul>
   *   <li>
   *     {@linkplain io.github.neonorbit.dexplore.reference.StringRefData}: string-literal.
   *   </li>
   *   <li>
   *     {@linkplain io.github.neonorbit.dexplore.reference.TypeRefData}:
   *     type name (eg: full class name).
   *   </li>
   *   <li>
   *     {@linkplain io.github.neonorbit.dexplore.reference.FieldRefData}:
   *     field name, field type, declaring class.
   *   </li>
   *   <li>
   *     {@linkplain io.github.neonorbit.dexplore.reference.MethodRefData}:
   *     method name, parameters, return type, declaring class.
   *   </li>
   * </ul>
   *
   * @param value the value to compare against
   * @return {@code true} if the reference contains the specified value
   *
   * @see StringRefData#contains(String)
   * @see TypeRefData#contains(String)
   * @see FieldRefData#contains(String)
   * @see MethodRefData#contains(String)
   */
  boolean contains(@Nonnull String value);
}
