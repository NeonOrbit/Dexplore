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

import io.github.neonorbit.dexplore.iface.Internal;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.analysis.reflection.util.ReflectionUtils;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedField;
import org.jf.dexlib2.dexbacked.DexBackedMethod;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Internal
public final class DexUtils {
  @Nonnull
  public static String javaToDexTypeName(@Nonnull String javaTypeName) {
    if (javaTypeName.isEmpty()) return javaTypeName;
    return ReflectionUtils.javaToDexName(javaTypeName);
  }

  @Nonnull
  public static List<String> javaToDexTypeName(@Nonnull Collection<String> javaTypeNames) {
    return javaTypeNames.stream()
                        .map(DexUtils::javaToDexTypeName)
                        .collect(Collectors.toList());
  }

  @Nonnull
  public static String dexToJavaTypeName(@Nonnull String dexTypeName) {
    if (dexTypeName.isEmpty()) return dexTypeName;
    return !(dexTypeName.charAt(0) == 'L') ? ReflectionUtils.dexToJavaName(dexTypeName) :
             dexTypeName.substring(1, dexTypeName.length() - 1).replace('/', '.');
  }

  @Nonnull
  public static List<String> dexToJavaTypeName(@Nonnull Collection<String> dexTypeNames) {
    return dexTypeNames.stream()
                       .map(DexUtils::dexToJavaTypeName)
                       .collect(Collectors.toList());
  }

  @Nonnull
  public static String javaClassToDexTypeName(@Nonnull Class<?> javaClass) {
    return javaToDexTypeName(javaClass.getName());
  }

  @Nonnull
  public static List<String> javaClassToDexTypeName(@Nonnull Collection<Class<?>> javaClasses) {
    return javaClasses.stream()
                      .map(DexUtils::javaClassToDexTypeName)
                      .collect(Collectors.toList());
  }

  @Nonnull
  public static String dexClassToJavaTypeName(@Nonnull DexBackedClassDef dexClass) {
    return dexToJavaTypeName(dexClass.getType());
  }

  @Nonnull
  public static List<String> dexClassToJavaTypeNames(@Nonnull Collection<DexBackedClassDef> dexClass) {
    return dexClass.stream()
                   .map(DexUtils::dexClassToJavaTypeName)
                   .collect(Collectors.toList());
  }

  @Nonnull
  public static String[] getJavaParams(@Nonnull DexBackedMethod dexMethod) {
    return dexToJavaTypeName(dexMethod.getParameterTypes()).toArray(new String[0]);
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

  public static Iterable<DexBackedMethod> dexMethods(DexBackedClassDef dexClass) {
    return dexMethods(dexClass, 0);
  }

  public static Iterable<DexBackedMethod> dexDirectMethods(DexBackedClassDef dexClass) {
    return dexMethods(dexClass, 1);
  }

  public static Iterable<DexBackedMethod> dexVirtualMethods(DexBackedClassDef dexClass) {
    return dexMethods(dexClass, 2);
  }

  public static Iterable<DexBackedField> dexFields(DexBackedClassDef dexClass) {
    return dexFields(dexClass, 0);
  }

  public static Iterable<DexBackedField> dexStaticFields(DexBackedClassDef dexClass) {
    return dexFields(dexClass, 1);
  }
  public static Iterable<DexBackedField> dexInstanceFields(DexBackedClassDef dexClass) {
    return dexFields(dexClass, 2);
  }

  private static boolean isValid(int flag) {
    return !AccessFlags.SYNTHETIC.isSet(flag);
  }

  @SuppressWarnings("unchecked")
  public static Iterable<DexBackedClassDef> dexClasses(DexBackedDexFile dexFile) {
    Iterable<DexBackedClassDef> it = (Iterable<DexBackedClassDef>) dexFile.getClasses();
    return new FilteredIterable<>(it.iterator(), clazz -> isValid(clazz.getAccessFlags()));
  }

  @SuppressWarnings("unchecked")
  private static Iterable<DexBackedMethod> dexMethods(DexBackedClassDef dexClass, int type) {
    Iterable<DexBackedMethod> it = (Iterable<DexBackedMethod>) (
                                      (type == 1) ? dexClass.getDirectMethods() :
                                      (type == 2) ? dexClass.getVirtualMethods() :
                                                    dexClass.getMethods()
                                   );
    return new FilteredIterable<>(it.iterator(), method -> isValid(method.getAccessFlags()));
  }

  @SuppressWarnings("unchecked")
  private static Iterable<DexBackedField> dexFields(DexBackedClassDef dexClass, int type) {
    Iterable<DexBackedField> it = (Iterable<DexBackedField>) (
                                      (type == 1) ? dexClass.getStaticFields() :
                                      (type == 2) ? dexClass.getInstanceFields() :
                                                    dexClass.getFields()
                                  );
    return new FilteredIterable<>(it.iterator(), field -> isValid(field.getAccessFlags()));
  }
}
