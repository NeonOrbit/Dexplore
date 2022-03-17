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

final class ReferencePoolBuffer {
  private boolean needsCopy;
  private List<TypeReferenceData> types;
  private List<StringReferenceData> strings;
  private List<FieldReferenceData> fields;
  private List<MethodReferenceData> methods;
  private final boolean fieldDetails, methodDetails;

  ReferencePoolBuffer(ReferenceTypes types) {
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
    types.forEach(TypeReferenceData::toString);
    strings.forEach(StringReferenceData::toString);
    fields.forEach(FieldReferenceData::toString);
    methods.forEach(MethodReferenceData::toString);
  }

  @Nonnull
  public ReferencePool getPool() {
    needsCopy = true;
    return new ReferencePool(types, strings, fields, methods);
  }

  @Nonnull
  public ReferencePool getPool(boolean resolve) {
    if (resolve) resolve();
    return getPool();
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
