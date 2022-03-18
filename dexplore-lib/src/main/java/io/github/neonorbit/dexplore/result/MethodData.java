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

package io.github.neonorbit.dexplore.result;

import io.github.neonorbit.dexplore.DexDecoder;
import io.github.neonorbit.dexplore.util.DexUtils;
import io.github.neonorbit.dexplore.ReferencePool;
import org.jf.dexlib2.dexbacked.DexBackedMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

public final class MethodData implements Comparable<MethodData> {
  @Nonnull public final String clazz;
  @Nonnull public final String method;
  @Nonnull public final String[] params;
  @Nonnull public final String returnType;
  @Nonnull private final ClassData classData;
  @Nonnull private final ReferencePool referencePool;

  private MethodData(@Nonnull String clazz,
                     @Nonnull String method,
                     @Nonnull String[] params,
                     @Nonnull String returnType,
                     @Nonnull ClassData classData,
                     @Nonnull ReferencePool referencePool) {
    this.clazz = clazz;
    this.method = method;
    this.params = params;
    this.returnType = returnType;
    this.classData = classData;
    this.referencePool = referencePool;
  }

  @Nullable
  public Method loadMethod(@Nonnull ClassLoader classLoader) {
    try {
      final Class<?>[] paramTypes = new Class[params.length];
      for (int i = 0; i < params.length; i++) {
        paramTypes[i] = classLoader.loadClass(params[i]);
      }
      return classLoader.loadClass(clazz).getDeclaredMethod(method, paramTypes);
    } catch (ClassNotFoundException | NoSuchMethodException e) {
      return null;
    }
  }

  @Nonnull
  public static MethodData from(@Nonnull DexBackedMethod dexMethod) {
    String signature = DexUtils.getMethodSignature(dexMethod);
    return ClassData.from(dexMethod.classDef).getMethod(signature);
  }

  @Nonnull
  public static MethodData from(@Nonnull DexBackedMethod dexMethod,
                                @Nullable ClassData sharedInstance) {
    if (sharedInstance == null) return from(dexMethod);
    String className = DexUtils.dexClassToJavaTypeName(dexMethod.classDef);
    if (!sharedInstance.clazz.equals(className)) {
      throw new IllegalArgumentException("Shared instance must be from the same class");
    }
    return new MethodData(className,
                            dexMethod.getName(),
                            DexUtils.getJavaParamList(dexMethod).toArray(new String[0]),
                            DexUtils.dexToJavaTypeName(dexMethod.getReturnType()),
                            sharedInstance,
                            DexDecoder.decodeFully(dexMethod));
  }

  @Nonnull
  public ClassData getClassResult() {
    return classData;
  }

  @Nonnull
  public ReferencePool getReferencePool() {
    return referencePool;
  }

  @Nonnull
  public String serialize() {
    StringJoiner joiner = new StringJoiner(":");
    joiner.add(clazz).add(method);
    for (String param : params) {
      joiner.add(param);
    }
    joiner.add(returnType);
    return joiner.toString();
  }

  @Nullable
  public static MethodData deserialize(@Nonnull String serialized) {
    try {
      final String[] raw = serialized.split(":");
      if (raw.length >= 3) {
        final int pIndex = Math.min(2, raw.length - 1);
        final String[] params = Arrays.copyOfRange(raw, pIndex, raw.length - 1);
        return new MethodData(raw[0], raw[1], params,
                                raw[raw.length - 1],
                                ClassData.deserialize(raw[0]),
                                ReferencePool.emptyPool());
      }
    } catch (Exception ignore) {}
    return null;
  }

  @Override
  public int compareTo(MethodData o) {
    return this.toString().compareTo(o.toString());
  }

  @Override
  public int hashCode() {
    return Objects.hash(clazz, method, Arrays.hashCode(params), returnType);
  }

  @Override
  public boolean equals(Object obj) {
    return (this == obj) || (obj instanceof MethodData) &&
           (this.clazz.equals(((MethodData) obj).clazz)) &&
           (this.method.equals(((MethodData) obj).method)) &&
           (this.returnType.equals(((MethodData) obj).returnType)) &&
           (Arrays.equals(this.params, ((MethodData) obj).params));
  }

  @Override
  public String toString() {
    return DexUtils.getMethodSignature(clazz, method, Arrays.asList(params), returnType);
  }
}
