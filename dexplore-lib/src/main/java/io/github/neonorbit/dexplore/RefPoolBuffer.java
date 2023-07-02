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

package io.github.neonorbit.dexplore;

import io.github.neonorbit.dexplore.reference.FieldRefData;
import io.github.neonorbit.dexplore.reference.MethodRefData;
import io.github.neonorbit.dexplore.reference.StringRefData;
import io.github.neonorbit.dexplore.reference.TypeRefData;
import io.github.neonorbit.dexplore.filter.ReferenceTypes;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.iface.reference.TypeReference;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

final class RefPoolBuffer {
  private List<StringRefData> strings = new ArrayList<>();
  private List<TypeRefData> types = new ArrayList<>();
  private List<FieldRefData> fields = new ArrayList<>();
  private List<MethodRefData> methods = new ArrayList<>();
  private final boolean fieldDetails, methodDetails;

  RefPoolBuffer(ReferenceTypes types) {
    this.fieldDetails = types.hasFieldDetails();
    this.methodDetails = types.hasMethodDetails();
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  private void resolve() {
    strings.forEach(StringRefData::toString);
    types.forEach(TypeRefData::toString);
    fields.forEach(FieldRefData::toString);
    methods.forEach(MethodRefData::toString);
  }

  @Nonnull
  public ReferencePool getPool() {
    ReferencePool pool = ReferencePool.build(strings, types, fields, methods);
    this.strings = null; this.types = null; this.fields = null; this.methods = null;
    return pool;
  }

  @Nonnull
  public ReferencePool getPool(boolean resolve) {
    if (resolve) resolve();
    return getPool();
  }

  public void add(@Nonnull String value) {
    strings.add(StringRefData.build(value));
  }

  public void add(@Nonnull StringReference value) {
    strings.add(StringRefData.build(value));
  }

  public void add(@Nonnull TypeReference value) {
    types.add(TypeRefData.build(value));
  }

  public void add(@Nonnull FieldReference value) {
    fields.add(FieldRefData.build(value, fieldDetails));
  }

  public void add(@Nonnull MethodReference value) {
    methods.add(MethodRefData.build(value, methodDetails));
  }
}
