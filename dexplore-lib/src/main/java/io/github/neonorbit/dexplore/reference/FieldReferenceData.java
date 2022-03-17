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
   */
  @Nonnull
  public String getName() {
    return getData().getName();
  }

  /**
   * Equivalent to {@link Field#getType()}
   */
  @Nonnull
  public String getType() {
    return getData().getType();
  }

  /**
   * Equivalent to {@link Field#getDeclaringClass()}
   */
  @Nonnull
  public String getDeclaringClass() {
    return getData().getDefiningClass();
  }

  @Override
  public boolean contains(@Nonnull String value) {
    final FieldReference ref = getData();
    return ref.getName().equals(value) ||
           details && (
              ref.getType().equals(value) ||
              ref.getDefiningClass().equals(value)
           );
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
   * Structure: className.fieldName:fieldType
   * <br><br>
   * Example: java.lang.Byte.SIZE:int
   */
  @Override
  public String toString() {
    if (signature == null) {
      FieldReference ref = getData();
      String name = ref.getName();
      String from = details ? ref.getDefiningClass() : "[blank]";
      String type = details ? ref.getType() : "[blank]";
      signature = DexUtils.getFieldSignature(from, name, type);
    }
    return signature;
  }
}
