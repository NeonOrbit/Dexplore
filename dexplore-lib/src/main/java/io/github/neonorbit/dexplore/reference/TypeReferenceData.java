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
