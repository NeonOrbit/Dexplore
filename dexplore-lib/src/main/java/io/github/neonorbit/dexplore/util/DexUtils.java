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
import org.jf.dexlib2.ValueType;
import org.jf.dexlib2.analysis.reflection.util.ReflectionUtils;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedField;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.iface.value.EncodedValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

@Internal
public final class DexUtils {
  public static boolean hasValue(@Nonnull DexBackedField dexField) {
    return hasValue(dexField.initialValue);
  }

  public static boolean hasValue(@Nullable EncodedValue encodedValue) {
    return encodedValue != null && encodedValue.getValueType() != ValueType.NULL;
  }

  @Nonnull
  public static String javaToDexTypeName(@Nonnull String javaTypeName) {
    return javaTypeName.isEmpty() ? javaTypeName : ReflectionUtils.javaToDexName(javaTypeName);
  }

  @Nonnull
  public static List<String> javaToDexTypeName(@Nonnull Collection<String> javaTypeNames) {
    return javaTypeNames.stream().map(DexUtils::javaToDexTypeName).collect(toList());
  }

  @Nonnull
  public static String dexToJavaTypeName(@Nonnull String dexTypeName) {
    if (dexTypeName.isEmpty()) return dexTypeName;
    return !(dexTypeName.charAt(0) == 'L') ? ReflectionUtils.dexToJavaName(dexTypeName) :
             dexTypeName.substring(1, dexTypeName.length() - 1).replace('/', '.');
  }

  @Nonnull
  public static List<String> dexToJavaTypeName(@Nonnull Collection<String> dexTypeNames) {
    return dexTypeNames.stream().map(DexUtils::dexToJavaTypeName).collect(toList());
  }

  @Nonnull
  public static String javaClassToDexTypeName(@Nonnull Class<?> javaClass) {
    return javaToDexTypeName(javaClass.getName());
  }

  @Nonnull
  public static List<String> javaClassToDexTypeName(@Nonnull Collection<Class<?>> javaClasses) {
    return javaClasses.stream().map(DexUtils::javaClassToDexTypeName).collect(toList());
  }

  @Nonnull
  public static String dexClassToJavaTypeName(@Nonnull DexBackedClassDef dexClass) {
    return dexToJavaTypeName(dexClass.getType());
  }

  @Nonnull
  public static List<String> dexClassToJavaTypeNames(@Nonnull Collection<DexBackedClassDef> dexClass) {
    return dexClass.stream().map(DexUtils::dexClassToJavaTypeName).collect(toList());
  }

  @Nonnull
  public static String[] getJavaParams(@Nonnull DexBackedMethod dexMethod) {
    return dexToJavaTypeName(dexMethod.getParameterTypes()).toArray(new String[0]);
  }

  @Nonnull
  public static String getFieldSignature(@Nonnull String name) {
    return "[blank]." + name + ":[blank]";
  }

  @Nonnull
  public static String getFieldSignature(@Nonnull DexBackedField dexField) {
    return getFieldSignature(
            dexToJavaTypeName(dexField.getDefiningClass()),
            dexField.getName(),
            dexToJavaTypeName(dexField.getType())
    );
  }

  @Nonnull
  public static String getFieldSignature(@Nonnull String definingClass,
                                         @Nonnull String fieldName,
                                         @Nonnull String fieldType) {
    return definingClass + '.' + fieldName + ':' + fieldType;
  }

  @Nonnull
  public static String getMethodSignature(@Nonnull String name) {
    return "[blank]." + name + "([blank]):[blank]";
  }

  @Nonnull
  public static String getMethodSignature(@Nonnull DexBackedMethod dexMethod) {
    return getMethodSignature(
            dexToJavaTypeName(dexMethod.getDefiningClass()),
            dexMethod.getName(),
            dexToJavaTypeName(dexMethod.getParameterTypes()),
            dexToJavaTypeName(dexMethod.getReturnType())
    );
  }

  @Nonnull
  public static String getMethodSignature(@Nonnull String definingClass,
                                          @Nonnull String methodName,
                                          @Nonnull Iterable<? extends CharSequence> params,
                                          @Nonnull String returnType) {
    return definingClass + '.' + methodName + '(' + String.join(",", params) + "):" + returnType;
  }

  public static Pattern javaToDexPattern(Pattern pattern) {
    String regex = pattern.pattern();
    if (regex.isEmpty()) return pattern;
    if ((pattern.flags() & Pattern.LITERAL) == Pattern.LITERAL) {
      return Pattern.compile(javaToDexTypeName(regex), pattern.flags());
    }
    String dexPattern = javaToDexPattern(regex);
    return regex.equals(dexPattern) ? pattern : Pattern.compile(dexPattern, pattern.flags());
  }

  private static String javaToDexPattern(String regex) {
    StringBuilder buffer = new StringBuilder(regex.length() + 2);
    boolean inEscape = false, inQuote = false;
    int brackets = 0;
    int i = -1;
    while (++i < regex.length()) {
      char current = regex.charAt(i);
      if (current == '\\') {
        if (inEscape) {
          buffer.append('\\');
          if (!inQuote) buffer.append('\\');
        }
        inEscape = inQuote || !inEscape;
        continue;
      }
      if (inQuote) {
        if (inEscape && current == 'E') inQuote = false;
      } else if (inEscape) {
        if (current == 'Q') inQuote = true;
      } else if (brackets > 0) {
        if (current == '[') brackets++;
        else if (current == ']') brackets--;
      } else {
        switch (current) {
          case '[': brackets++; break;
          case '$': buffer.append(';'); break;
          case '^': buffer.append(current); current = 'L'; break;
        }
      }
      if (current == '.' && (inEscape || inQuote || brackets > 0)) {
        if (!inQuote) inEscape = false;
        current = '/';
      }
      if (inEscape) {
        inEscape = false;
        buffer.append('\\');
      }
      buffer.append(current);
    }
    return buffer.toString();
  }

  public static boolean skipSynthetic(boolean isSynthEnabled, int flags) {
    return !isSynthEnabled && AccessFlags.SYNTHETIC.isSet(flags);
  }

  @SuppressWarnings("unchecked")
  public static Iterable<DexBackedMethod> dexMethods(DexBackedClassDef dexClass, boolean synthEnabled) {
    return synthEnabled ? (Iterable<DexBackedMethod>) dexClass.getMethods() : filterSynthMethods(dexClass, 0);
  }

  @SuppressWarnings("unchecked")
  public static Iterable<DexBackedMethod> dexDirectMethods(DexBackedClassDef dexClass, boolean synthEnabled) {
    return synthEnabled ? (Iterable<DexBackedMethod>) dexClass.getDirectMethods() : filterSynthMethods(dexClass, 1);
  }

  @SuppressWarnings("unchecked")
  public static Iterable<DexBackedMethod> dexVirtualMethods(DexBackedClassDef dexClass, boolean synthEnabled) {
    return synthEnabled ? (Iterable<DexBackedMethod>) dexClass.getVirtualMethods() : filterSynthMethods(dexClass, 2);
  }

  public static Iterable<DexBackedField> dexStaticFields(DexBackedClassDef dexClass) {
    return dexStaticFields(dexClass, false);
  }

  @SuppressWarnings("unchecked")
  public static Iterable<DexBackedField> dexFields(DexBackedClassDef dexClass, boolean synthEnabled) {
    return synthEnabled ? (Iterable<DexBackedField>) dexClass.getFields() : filterSynthFields(dexClass, 0);
  }

  @SuppressWarnings("unchecked")
  public static Iterable<DexBackedField> dexStaticFields(DexBackedClassDef dexClass, boolean synthEnabled) {
    return synthEnabled ? (Iterable<DexBackedField>) dexClass.getStaticFields() : filterSynthFields(dexClass, 1);
  }

  @SuppressWarnings("unchecked")
  public static Iterable<DexBackedField> dexInstanceFields(DexBackedClassDef dexClass, boolean synthEnabled) {
    return synthEnabled ? (Iterable<DexBackedField>) dexClass.getInstanceFields() : filterSynthFields(dexClass, 2);
  }

  /**
   * @param dexClass source
   * @param type 1: direct, 2: virtual
   * @return filtered dex methods
   */
  @SuppressWarnings("unchecked")
  private static Iterable<DexBackedMethod> filterSynthMethods(DexBackedClassDef dexClass, int type) {
    Iterable<DexBackedMethod> it = (Iterable<DexBackedMethod>) (
            (type==1) ? dexClass.getDirectMethods() : (type==2) ? dexClass.getVirtualMethods() : dexClass.getMethods()
    );
    return new FilteredIterable<>(it, method -> !AccessFlags.SYNTHETIC.isSet(method.accessFlags));
  }

  /**
   * @param dexClass source
   * @param type 1: static, 2: instance
   * @return filtered dex fields
   */
  @SuppressWarnings("unchecked")
  private static Iterable<DexBackedField> filterSynthFields(DexBackedClassDef dexClass, int type) {
    Iterable<DexBackedField> it = (Iterable<DexBackedField>) (
            (type==1) ? dexClass.getStaticFields() : (type==2) ? dexClass.getInstanceFields() : dexClass.getFields()
    );
    return new FilteredIterable<>(it, field -> !AccessFlags.SYNTHETIC.isSet(field.accessFlags));
  }
}
