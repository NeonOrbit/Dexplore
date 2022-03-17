package io.github.neonorbit.dexplore.reference;

import io.github.neonorbit.dexplore.util.DexUtils;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class MethodReferenceData implements DexReferenceData {
  private final boolean details;
  private boolean resolved;
  private String signature;
  private MethodReference data;

  private MethodReferenceData(MethodReference reference, boolean details) {
    this.details = details;
    this.data = reference;
  }

  public static MethodReferenceData build(MethodReference reference, boolean details) {
    return new MethodReferenceData(reference, details);
  }

  private MethodReference getData() {
    if (!resolved) {
      resolved = true;
      String name = data.getName();
      String from = details ? DexUtils.dexToJavaTypeName(data.getDefiningClass()) : "";
      String type = details ? DexUtils.dexToJavaTypeName(data.getReturnType()) : "";
      List<String> param = details ? DexUtils.dexToJavaTypeName(getParamList(data)) : null;
      data = new ImmutableMethodReference(from, name, param, type);
    }
    return data;
  }

  /**
   * Equivalent to {@link Method#getName()}
   */
  @Nonnull
  public String getName() {
    return getData().getName();
  }

  /**
   * Equivalent to {@link Method#getReturnType()}
   */
  @Nonnull
  public String getReturnType() {
    return getData().getReturnType();
  }

  /**
   * Equivalent to {@link Method#getParameterTypes()}
   */
  @Nonnull
  public List<String> getParameterTypes() {
    return getParamList(getData());
  }

  /**
   * Equivalent to {@link Method#getDeclaringClass()}
   */
  @Nonnull
  public String getDeclaringClass() {
    return getData().getDefiningClass();
  }

  @Override
  public boolean contains(@Nonnull String value) {
    final MethodReference ref = getData();
    return ref.getName().equals(value) ||
           details && (
              ref.getDefiningClass().equals(value) ||
              ref.getReturnType().equals(value) ||
              ref.getParameterTypes().stream().anyMatch(cs -> cs.toString().equals(value))
           );
  }

  @SuppressWarnings("unchecked")
  private static List<String> getParamList(MethodReference reference) {
    List<? extends CharSequence> list = reference.getParameterTypes();
    try {
      return (List<String>) list;
    } catch (ClassCastException ignore) {
      return list.stream().map(CharSequence::toString).collect(Collectors.toList());
    }
  }

  @Override
  public int hashCode() {
    return getData().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return (this == obj) || (obj instanceof MethodReferenceData) &&
           (this.getData().equals(((MethodReferenceData) obj).getData()));
  }

  /**
   * Structure: className.methodName(param1,param2...paramN):returnType
   * <br><br>
   * Example: java.lang.String.indexOf(java.lang.String,int):int
   */
  @Override
  public String toString() {
    if (signature == null) {
      MethodReference ref = getData();
      String name = ref.getName();
      String from = details ? ref.getDefiningClass() : "[blank]";
      String type = details ? ref.getReturnType() : "[blank]";
      String para = details ? String.join(",", ref.getParameterTypes()) : "[blank]";
      signature = DexUtils.getMethodSignature(from, name, Collections.singleton(para), type);
    }
    return signature;
  }
}
