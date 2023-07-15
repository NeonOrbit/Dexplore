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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toSet;

final class FilterUtils {
  public static boolean containsAllAnnotations(@Nonnull DexBackedClassDef dexClass,
                                               @Nonnull Set<String> annotations, boolean synthetic) {
    return containsAllAnnotations(getAllAnnotations(dexClass, synthetic), annotations);
  }

  public static boolean containsAllAnnotationValues(@Nonnull DexBackedClassDef dexClass,
                                                    @Nonnull Set<String> annotationValues, boolean synthetic) {
    return containsAllAnnotationValues(getAllAnnotations(dexClass, synthetic), annotationValues);
  }

  public static boolean containsAllAnnotations(@Nonnull DexBackedMethod dexMethod,
                                               @Nonnull Set<String> annotations) {
    Set<? extends Annotation> source = dexMethod.getAnnotations();
    return source.size() >= annotations.size() && containsAllAnnotations(source.stream(), annotations);
  }

  public static boolean containsAllAnnotationValues(@Nonnull DexBackedMethod dexMethod,
                                                    @Nonnull Set<String> annotationValues) {
    return containsAllAnnotationValues(dexMethod.getAnnotations().stream(), annotationValues);
  }

  private static boolean containsAllAnnotations(@Nonnull Stream<? extends Annotation> source,
                                                @Nonnull Set<String> annotations) {
    Set<String> combinedSource = source.map(Annotation::getType).collect(toSet());
    return combinedSource.size() >= annotations.size() && combinedSource.containsAll(annotations);
  }

  private static boolean containsAllAnnotationValues(@Nonnull Stream<? extends Annotation> source,
                                                     @Nonnull Set<String> annotationValues) {
    Set<String> values = new HashSet<>();
    source.forEach(annotation -> {
      if (annotation.getType().startsWith("Ldalvik/annotation/")) return;
      for (AnnotationElement element : annotation.getElements()) {
        decode(element.getValue(), values);
      }
    });
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

  private static Stream<Annotation> getAllAnnotations(DexBackedClassDef dexClass, boolean synthetic) {
    Stream<Annotation> members = Stream.concat(
            StreamSupport.stream(DexUtils.dexMethods(dexClass, synthetic).spliterator(), false)
                    .flatMap(m -> m.getAnnotations().stream()),
            StreamSupport.stream(DexUtils.dexFields(dexClass, synthetic).spliterator(), false)
                    .flatMap(f -> f.getAnnotations().stream())
    );
    return Stream.concat(dexClass.getAnnotations().stream(), members);
  }
}
