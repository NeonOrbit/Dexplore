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
import io.github.neonorbit.dexplore.ReferencePool;
import io.github.neonorbit.dexplore.iface.Internal;
import io.github.neonorbit.dexplore.reference.StringRefData;
import io.github.neonorbit.dexplore.util.DexUtils;
import org.jf.dexlib2.AccessFlags;
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
  public static ClassData ofClass(@Nonnull DexBackedClassDef dexClass, boolean synthetic) {
    return buildClassData(dexClass, null, synthetic);
  }

  public static MethodData ofMethod(@Nonnull DexBackedMethod dexMethod, boolean synthetic) {
    return ofMethod(null, dexMethod, synthetic);
  }

  public static MethodData ofMethod(@Nullable ClassData sharedInstance,
                                    @Nonnull DexBackedMethod dexMethod, boolean synthetic) {
    if (sharedInstance == null || sharedInstance.getMethods().isEmpty() ||
            !DexUtils.dexClassToJavaTypeName(dexMethod.classDef).equals(sharedInstance.clazz)) {
      sharedInstance = buildClassData(dexMethod.classDef, dexMethod, synthetic);
    }
    MethodData method = sharedInstance.getMethodBySignature(DexUtils.getMethodSignature(dexMethod));
    if (method == null) {
      throw new AssertionError();
    }
    return method;
  }

  private static ClassData buildClassData(@Nonnull DexBackedClassDef dexClass,
                                          @Nullable DexBackedMethod forMethod, boolean synthetic) {
    String clazz = DexUtils.dexClassToJavaTypeName(dexClass);
    ClassData instance = new ClassData(clazz);
    List<FieldData> fields = new ArrayList<>();
    DexUtils.dexFields(dexClass, synthetic).forEach(dexField -> {
      FieldData fieldData = buildFieldData(instance, dexField);
      fields.add(fieldData);
    });
    Map<String, MethodData> methods = new LinkedHashMap<>();
    DexUtils.dexMethods(dexClass, synthetic).forEach(dexMethod -> {
      MethodData method = buildMethodData(instance, dexMethod);
      methods.put(method.getSignature(), method);
    });
    if (!synthetic && forMethod != null && AccessFlags.SYNTHETIC.isSet(forMethod.accessFlags)) {
      MethodData method = buildMethodData(instance, forMethod);
      methods.put(method.getSignature(), method);
    }
    instance.setModifier(dexClass.getAccessFlags());
    instance.setFields(Collections.unmodifiableList(fields));
    instance.setMethods(Collections.unmodifiableMap(methods));
    return instance;
  }

  private static MethodData buildMethodData(@Nonnull ClassData sharedInstance,
                                            @Nonnull DexBackedMethod dexMethod) {
    MethodData instance = new MethodData(
            sharedInstance.clazz,
            dexMethod.getName(),
            DexUtils.getJavaParams(dexMethod),
            DexUtils.dexToJavaTypeName(dexMethod.getReturnType())
    );
    instance.setClassData(sharedInstance);
    instance.setModifier(dexMethod.accessFlags);
    instance.setReferencePool(DexDecoder.decodeFully(dexMethod));
    return instance;
  }

  private static FieldData buildFieldData(@Nonnull ClassData sharedInstance,
                                          @Nonnull DexBackedField dexField) {
    FieldData instance = new FieldData(
            sharedInstance.clazz,
            dexField.getName(),
            DexUtils.dexToJavaTypeName(dexField.getType())
    );
    instance.setModifier(dexField.accessFlags);
    if (DexUtils.hasValue(dexField)) {
      ReferencePool pool = DexDecoder.decodeFully(dexField);
      List<StringRefData> strings = pool.getStringSection();
      instance.setValue(
              !strings.isEmpty() ? strings.get(0).getString() : DexDecoder.decodeFieldValue(dexField)
      );
      instance.setReferencePool(pool);
    }
    return instance;
  }
}
