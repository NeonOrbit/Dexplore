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
