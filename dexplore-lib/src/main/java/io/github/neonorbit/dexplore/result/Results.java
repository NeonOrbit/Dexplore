package io.github.neonorbit.dexplore.result;

import io.github.neonorbit.dexplore.DexDecoder;
import io.github.neonorbit.dexplore.util.DexUtils;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Results {
  public static ClassData ofClass(@Nonnull DexBackedClassDef dexClass) {
    return buildClassData(dexClass);
  }

  public static MethodData ofMethod(@Nonnull DexBackedMethod dexMethod) {
    return ofMethod(dexMethod, null);
  }

  public static MethodData ofMethod(@Nonnull DexBackedMethod dexMethod,
                                    @Nullable ClassData sharedInstance) {
    ClassData shared = sharedInstance;
    if (shared == null || shared.getMethods().isEmpty() ||
        !DexUtils.dexClassToJavaTypeName(dexMethod.classDef).equals(shared.clazz)) {
      shared = buildClassData(dexMethod.classDef);
    }
    MethodData method = shared.getMethod(DexUtils.getMethodSignature(dexMethod));
    if (method == null) {
      throw new AssertionError();
    }
    return method;
  }

  private static ClassData buildClassData(@Nonnull DexBackedClassDef dexClass) {
    String clazz = DexUtils.dexClassToJavaTypeName(dexClass);
    ClassData instance = new ClassData(clazz);
    Map<String, MethodData> map = new HashMap<>();
    for (DexBackedMethod dexMethod : dexClass.getMethods()) {
      if (!AccessFlags.SYNTHETIC.isSet(dexMethod.accessFlags)) {
        MethodData method = buildMethodData(dexMethod, instance);
        map.put(method.getSignature(), method);
      }
    }
    instance.setMethods(Collections.unmodifiableMap(map));
    instance.setReferencePool(DexDecoder.decodeFully(dexClass));
    return instance;
  }

  private static MethodData buildMethodData(@Nonnull DexBackedMethod dexMethod,
                                            @Nonnull ClassData sharedInstance) {
    MethodData instance = new MethodData(
                            sharedInstance.clazz,
                            dexMethod.getName(),
                            DexUtils.getJavaParams(dexMethod),
                            DexUtils.dexToJavaTypeName(dexMethod.getReturnType())
                          );
    instance.setClassData(sharedInstance);
    instance.setReferencePool(DexDecoder.decodeFully(dexMethod));
    return instance;
  }
}
