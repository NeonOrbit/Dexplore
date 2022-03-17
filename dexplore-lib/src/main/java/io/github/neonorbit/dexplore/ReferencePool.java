package io.github.neonorbit.dexplore;

import io.github.neonorbit.dexplore.reference.FieldReferenceData;
import io.github.neonorbit.dexplore.reference.MethodReferenceData;
import io.github.neonorbit.dexplore.reference.StringReferenceData;
import io.github.neonorbit.dexplore.reference.TypeReferenceData;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

public final class ReferencePool {
  private final List<TypeReferenceData> types;
  private final List<StringReferenceData> strings;
  private final List<FieldReferenceData> fields;
  private final List<MethodReferenceData> methods;

  ReferencePool(List<TypeReferenceData> types,
                List<StringReferenceData> strings,
                List<FieldReferenceData> fields,
                List<MethodReferenceData> methods) {
    this.types = Collections.unmodifiableList(types);
    this.strings = Collections.unmodifiableList(strings);
    this.fields = Collections.unmodifiableList(fields);
    this.methods = Collections.unmodifiableList(methods);
  }

  public static ReferencePool emptyPool() {
    return new ReferencePool(Collections.emptyList(),
                             Collections.emptyList(),
                             Collections.emptyList(),
                             Collections.emptyList());
  }

  public boolean isEmpty() {
    return types.isEmpty() &&
           strings.isEmpty() &&
           fields.isEmpty() &&
           methods.isEmpty();
  }

  @Nonnull
  public List<StringReferenceData> getStringSection() {
    return strings;
  }

  @Nonnull
  public List<FieldReferenceData> getFieldSection() {
    return fields;
  }

  @Nonnull
  public List<MethodReferenceData> getMethodSection() {
    return methods;
  }

  @Nonnull
  public List<TypeReferenceData> getTypeSection() {
    return types;
  }

  public boolean contains(@Nonnull final String value) {
    return stringsContain(value) ||
           fieldsContain(value)  ||
           methodsContain(value) ||
           typesContain(value);
  }

  public boolean stringsContain(@Nonnull String value) {
    return strings.stream().anyMatch(s -> s.contains(value));
  }

  public boolean fieldsContain(@Nonnull String value) {
    return fields.stream().anyMatch(f -> f.contains(value));
  }

  public boolean methodsContain(@Nonnull String value) {
    return methods.stream().anyMatch(m -> m.contains(value));
  }

  public boolean typesContain(@Nonnull String value) {
    return types.stream().anyMatch(t -> t.contains(value));
  }

  public boolean fieldSignaturesContain(@Nonnull String signature) {
    return fields.stream().anyMatch(f -> f.toString().contains(signature));
  }

  public boolean methodSignaturesContain(@Nonnull String signature) {
    return methods.stream().anyMatch(m -> m.toString().contains(signature));
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner("\n");
    strings.forEach(s -> joiner.add(s.toString()));
    fields.forEach(f -> joiner.add(f.toString()));
    methods.forEach(m -> joiner.add(m.toString()));
    types.forEach(t -> joiner.add(t.toString()));
    return joiner.toString();
  }
}
