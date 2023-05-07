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

package io.github.neonorbit.dexplore.filter;

import io.github.neonorbit.dexplore.util.DexUtils;
import org.jf.dexlib2.ValueType;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.value.AnnotationEncodedValue;
import org.jf.dexlib2.iface.value.ArrayEncodedValue;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.iface.value.StringEncodedValue;
import org.jf.dexlib2.iface.value.TypeEncodedValue;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

final class FilterUtils {
  public static boolean containsAllAnnotations(@Nonnull DexBackedClassDef dexClass,
                                               @Nonnull Set<String> annotations) {
    return containsAllAnnotations(dexClass.getAnnotations(), annotations);
  }

  public static boolean containsAllAnnotationValues(@Nonnull DexBackedClassDef dexClass,
                                                    @Nonnull Set<String> annotationValues) {
    return containsAllAnnotationValues(dexClass.getAnnotations(), annotationValues);
  }

  public static boolean containsAllAnnotations(@Nonnull DexBackedMethod dexMethod,
                                               @Nonnull Set<String> annotations) {
    return containsAllAnnotations(dexMethod.getAnnotations(), annotations);
  }

  public static boolean containsAllAnnotationValues(@Nonnull DexBackedMethod dexMethod,
                                                    @Nonnull Set<String> annotationValues) {
    return containsAllAnnotationValues(dexMethod.getAnnotations(), annotationValues);
  }

  public static boolean containsAllAnnotations(@Nonnull Set<? extends Annotation> reader,
                                               @Nonnull Set<String> annotations) {
    if (reader.size() < annotations.size()) return false;
    return reader.stream().map(Annotation::getType)
                 .collect(Collectors.toSet()).containsAll(annotations);
  }

  public static boolean containsAllAnnotationValues(@Nonnull Set<? extends Annotation> reader,
                                                    @Nonnull Set<String> annotationValues) {
    Set<String> values = new HashSet<>();
    for (Annotation annot : reader) {
      if (annot.getType().startsWith("Ldalvik/annotation/")) continue;
      for (AnnotationElement element : annot.getElements()) {
        decode(element.getValue(), values);
      }
    }
    return values.containsAll(annotationValues);
  }

  private static void decode(@Nonnull EncodedValue encoded, @Nonnull Set<String> writer) {
    switch (encoded.getValueType()) {
      case ValueType.STRING:
        writer.add(((StringEncodedValue)encoded).getValue());
        break;
      case ValueType.TYPE:
        writer.add(DexUtils.dexToJavaTypeName(((TypeEncodedValue)encoded).getValue()));
        break;
      case ValueType.ARRAY:
        for (EncodedValue e : ((ArrayEncodedValue)encoded).getValue()) {
          decode(e, writer);
        }
        break;
      case ValueType.ANNOTATION:
        for (AnnotationElement e : ((AnnotationEncodedValue)encoded).getElements()) {
          decode(e.getValue(), writer);
        }
        break;
      default:
        break;
    }
  }
}
