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

package io.github.neonorbit.dexplore.util;

import org.jf.dexlib2.analysis.reflection.util.ReflectionUtils;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedField;
import org.jf.dexlib2.dexbacked.DexBackedMethod;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class DexUtils {
  @Nonnull
  public static String javaToDexTypeName(@Nonnull String javaTypeName) {
    return ReflectionUtils.javaToDexName(javaTypeName);
  }

  @Nonnull
  public static List<String> javaToDexTypeName(@Nonnull Collection<String> javaTypeNames) {
    return javaTypeNames.stream().sequential()
                        .map(DexUtils::javaToDexTypeName)
                        .collect(Collectors.toList());
  }

  @Nonnull
  public static String dexToJavaTypeName(@Nonnull String dexTypeName) {
    return !(dexTypeName.charAt(0) == 'L') ? ReflectionUtils.dexToJavaName(dexTypeName) :
            dexTypeName.substring(1, dexTypeName.length() - 1).replace('/', '.');
  }

  @Nonnull
  public static List<String> dexToJavaTypeName(@Nonnull Collection<String> dexTypeNames) {
    return dexTypeNames.stream().sequential()
                       .map(DexUtils::dexToJavaTypeName)
                       .collect(Collectors.toList());
  }

  @Nonnull
  public static String javaClassToDexTypeName(@Nonnull Class<?> javaClass) {
    return javaToDexTypeName(javaClass.getName());
  }

  @Nonnull
  public static List<String> javaClassToDexTypeName(@Nonnull Collection<Class<?>> javaClasses) {
    return javaClasses.stream().sequential()
                      .map(DexUtils::javaClassToDexTypeName)
                      .collect(Collectors.toList());
  }

  @Nonnull
  public static String dexClassToJavaTypeName(@Nonnull DexBackedClassDef dexClass) {
    return dexToJavaTypeName(dexClass.getType());
  }

  @Nonnull
  public static List<String> dexClassToJavaTypeNames(@Nonnull Collection<DexBackedClassDef> dexClass) {
    return dexClass.stream().sequential()
                   .map(DexUtils::dexClassToJavaTypeName)
                   .collect(Collectors.toList());
  }

  @Nonnull
  public static List<String> getJavaParamList(@Nonnull DexBackedMethod dexMethod) {
    return dexToJavaTypeName(dexMethod.getParameterTypes());
  }

  @Nonnull
  public static String getFieldSignature(@Nonnull DexBackedField dexField) {
    return getFieldSignature(dexToJavaTypeName(dexField.getDefiningClass()),
                             dexField.getName(),
                             dexToJavaTypeName(dexField.getType()));
  }

  @Nonnull
  public static String getFieldSignature(@Nonnull String definingClass,
                                         @Nonnull String fieldName,
                                         @Nonnull String fieldType) {
    return definingClass + '.' + fieldName + ':' + fieldType;
  }

  @Nonnull
  public static String getMethodSignature(@Nonnull DexBackedMethod dexMethod) {
    return getMethodSignature(dexToJavaTypeName(dexMethod.getDefiningClass()),
                              dexMethod.getName(),
                              dexToJavaTypeName(dexMethod.getParameterTypes()),
                              dexToJavaTypeName(dexMethod.getReturnType()));
  }

  @Nonnull
  public static String getMethodSignature(@Nonnull String definingClass,
                                          @Nonnull String methodName,
                                          @Nonnull Iterable<? extends CharSequence> params,
                                          @Nonnull String returnType) {
    return definingClass + '.' + methodName + '(' + String.join(",", params) + "):" + returnType;
  }
}
