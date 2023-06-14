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
import io.github.neonorbit.dexplore.iface.Internal;
import io.github.neonorbit.dexplore.util.DexUtils;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedField;
import org.jf.dexlib2.dexbacked.DexBackedMethod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Internal
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
    MethodData method = shared.getMethodBySignature(DexUtils.getMethodSignature(dexMethod));
    if (method == null) {
      throw new AssertionError();
    }
    return method;
  }

  private static ClassData buildClassData(@Nonnull DexBackedClassDef dexClass) {
    String clazz = DexUtils.dexClassToJavaTypeName(dexClass);
    ClassData instance = new ClassData(clazz);
    Map<String, MethodData> methods = new LinkedHashMap<>();
    DexUtils.dexMethods(dexClass).forEach(dexMethod -> {
      MethodData method = buildMethodData(dexMethod, instance);
      methods.put(method.getSignature(), method);
    });
    List<FieldData> fields = new ArrayList<>();
    DexUtils.dexFields(dexClass).forEach(dexField -> {
      FieldData fieldData = buildFieldData(dexField, instance);
      fields.add(fieldData);
    });
    instance.setFields(Collections.unmodifiableList(fields));
    instance.setMethods(Collections.unmodifiableMap(methods));
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

  private static FieldData buildFieldData(@Nonnull DexBackedField dexField,
                                          @Nonnull ClassData sharedInstance) {
    FieldData instance = new FieldData(
            sharedInstance.clazz,
            dexField.getName(),
            DexUtils.dexToJavaTypeName(dexField.getType())
    );
    instance.setValue(DexDecoder.decodeFieldValue(dexField));
    instance.setReferencePool(DexDecoder.decodeFully(dexField));
    return instance;
  }
}
