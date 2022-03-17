package io.github.neonorbit.dexplore.result;

import io.github.neonorbit.dexplore.DexDecoder;
import io.github.neonorbit.dexplore.ReferencePool;
import io.github.neonorbit.dexplore.util.DexUtils;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ClassData implements Comparable<ClassData> {
  @Nonnull public final String clazz;
  @Nonnull private final ReferencePool refPool;
  @Nonnull private final Map<String, MethodData> methods;

  protected ClassData(@Nonnull String clazz,
                      @Nonnull ReferencePool refPool,
                      @Nonnull Map<String, MethodData> methods) {
    this.clazz = clazz;
    this.refPool = refPool;
    this.methods = methods;
  }

  @Nullable
  public Class<?> loadClass(@Nonnull ClassLoader classLoader) {
    try {
      return classLoader.loadClass(clazz);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  @Nonnull
  public static ClassData from(@Nonnull final DexBackedClassDef dexClass) {
    String name = DexUtils.dexClassToJavaTypeName(dexClass);
    ReferencePool pool = DexDecoder.decodeFully(dexClass);
    Map<String, MethodData> methods = new HashMap<>();
    ClassData instance = new ClassData(name, pool, methods);
    dexClass.getMethods().forEach(dexMethod -> {
      if (!AccessFlags.SYNTHETIC.isSet(dexMethod.accessFlags)) {
        MethodData method = MethodData.from(dexMethod, instance);
        methods.put(method.toString(), method);
      }
    });
    return instance;
  }

  @Nonnull
  public MethodData getMethod(String signature) {
    return methods.get(signature);
  }

  @Nonnull
  public Set<MethodData> getMethods() {
    return Collections.unmodifiableSet(new HashSet<>(methods.values()));
  }

  @Nonnull
  public ReferencePool getReferencePool() {
    return refPool;
  }

  @Nonnull
  public String serialize() {
    return toString();
  }

  @Nonnull
  public static ClassData deserialize(@Nonnull String serialized) {
    Objects.requireNonNull(serialized);
    return new ClassData(serialized, ReferencePool.emptyPool(), Collections.emptyMap());
  }

  @Override
  public int compareTo(ClassData o) {
    return this.toString().compareTo(o.toString());
  }

  @Override
  public int hashCode() {
    return clazz.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return (this == obj) || (obj instanceof ClassData) &&
           (this.clazz.equals(((ClassData) obj).clazz));
  }

  @Override
  public String toString() {
    return clazz;
  }
}
