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

import io.github.neonorbit.dexplore.reference.FieldReferenceData;
import io.github.neonorbit.dexplore.reference.MethodReferenceData;
import io.github.neonorbit.dexplore.reference.StringReferenceData;
import io.github.neonorbit.dexplore.reference.TypeReferenceData;
import io.github.neonorbit.dexplore.filter.ReferenceTypes;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.iface.reference.TypeReference;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

final class RefPoolRBuffer {
  private boolean needsCopy;
  private List<StringReferenceData> strings;
  private List<TypeReferenceData> types;
  private List<FieldReferenceData> fields;
  private List<MethodReferenceData> methods;
  private final boolean fieldDetails, methodDetails;

  RefPoolRBuffer(ReferenceTypes types) {
    this.strings = new ArrayList<>();
    this.types = new ArrayList<>();
    this.fields = new ArrayList<>();
    this.methods = new ArrayList<>();
    this.fieldDetails = types.hasFieldDetails();
    this.methodDetails = types.hasMethodDetails();
  }

  private void update() {
    if (needsCopy) {
      needsCopy = false;
      strings = new ArrayList<>(strings);
      types = new ArrayList<>(types);
      fields = new ArrayList<>(fields);
      methods = new ArrayList<>(methods);
    }
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  private void resolve() {
    strings.forEach(StringReferenceData::toString);
    types.forEach(TypeReferenceData::toString);
    fields.forEach(FieldReferenceData::toString);
    methods.forEach(MethodReferenceData::toString);
  }

  @Nonnull
  public ReferencePool getPool() {
    needsCopy = true;
    return ReferencePool.build(strings, types, fields, methods);
  }

  @Nonnull
  public ReferencePool getPool(boolean resolve) {
    if (resolve) resolve();
    return getPool();
  }

  public void add(@Nonnull String value) {
    update();
    strings.add(StringReferenceData.build(value));
  }

  public void add(@Nonnull StringReference value) {
    update();
    strings.add(StringReferenceData.build(value));
  }

  public void add(@Nonnull TypeReference value) {
    update();
    types.add(TypeReferenceData.build(value));
  }

  public void add(@Nonnull FieldReference value) {
    update();
    fields.add(FieldReferenceData.build(value, fieldDetails));
  }

  public void add(@Nonnull MethodReference value) {
    update();
    methods.add(MethodReferenceData.build(value, methodDetails));
  }
}
