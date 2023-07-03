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

package io.github.neonorbit.dexplore;

import io.github.neonorbit.dexplore.filter.ReferenceTypes;
import io.github.neonorbit.dexplore.iface.Internal;
import io.github.neonorbit.dexplore.util.DexLog;
import io.github.neonorbit.dexplore.util.DexUtils;
import org.jf.dexlib2.ValueType;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedField;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.instruction.DualReferenceInstruction;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.instruction.WideLiteralInstruction;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.iface.reference.TypeReference;
import org.jf.dexlib2.iface.value.BooleanEncodedValue;
import org.jf.dexlib2.iface.value.ByteEncodedValue;
import org.jf.dexlib2.iface.value.CharEncodedValue;
import org.jf.dexlib2.iface.value.DoubleEncodedValue;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.iface.value.FloatEncodedValue;
import org.jf.dexlib2.iface.value.IntEncodedValue;
import org.jf.dexlib2.iface.value.LongEncodedValue;
import org.jf.dexlib2.iface.value.ShortEncodedValue;
import org.jf.dexlib2.iface.value.StringEncodedValue;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Internal
public final class DexDecoder {
  private final boolean cache;
  private final RefPoolCache<DexEntry> dexCache;
  private final RefPoolCache<DexBackedClassDef> classCache;

  DexDecoder(DexOptions options) {
    this.cache = options.enableCache;
    this.dexCache = new RefPoolCache<>();
    this.classCache = new RefPoolCache<>();
  }

  @Nonnull
  public ReferencePool decode(@Nonnull DexEntry dexEntry,
                              @Nonnull ReferenceTypes types) {
    if (types.hasNone()) return ReferencePool.emptyPool();
    ReferencePool pool = cache ? dexCache.get(dexEntry, types) : null;
    if (pool == null) {
      pool = decodeDexReferences(dexEntry.getDexFile(), types, false);
      if (cache) dexCache.put(dexEntry, types, pool);
    }
    return pool;
  }

  @Nonnull
  public ReferencePool decode(@Nonnull DexBackedClassDef dexClass,
                              @Nonnull ReferenceTypes types) {
    if (types.hasNone()) return ReferencePool.emptyPool();
    ReferencePool pool = cache ? classCache.get(dexClass, types) : null;
    if (pool == null) {
      pool = decodeClassReferences(dexClass, types, false);
      if (cache) classCache.put(dexClass, types, pool);
    }
    return pool;
  }

  @Nonnull
  public ReferencePool decode(@Nonnull DexBackedMethod dexMethod,
                              @Nonnull ReferenceTypes types) {
    return decodeMethodReferences(dexMethod, types, false);
  }

  @Nonnull
  public static ReferencePool decodeFully(@Nonnull DexBackedDexFile dexFile) {
    return decodeDexReferences(dexFile, ReferenceTypes.all(), true);
  }

  @Nonnull
  public static ReferencePool decodeFully(@Nonnull DexBackedClassDef dexClass) {
    return decodeClassReferences(dexClass, ReferenceTypes.all(), true);
  }

  @Nonnull
  public static ReferencePool decodeFully(@Nonnull DexBackedField dexField) {
    if (!DexUtils.hasValue(dexField)) return ReferencePool.emptyPool();
    RefPoolBuffer buffer = new RefPoolBuffer(ReferenceTypes.all());
    decodeFieldReferences(dexField, buffer);
    return buffer.getPool(true);
  }

  @Nonnull
  public static ReferencePool decodeFully(@Nonnull DexBackedMethod dexMethod) {
    return decodeMethodReferences(dexMethod, ReferenceTypes.all(), true);
  }

  public static Set<Long> decodeNumberLiterals(@Nonnull DexBackedClassDef dexClass) {
    Set<Long> numbers = new HashSet<>();
    DexUtils.dexStaticFields(dexClass).forEach(f -> decodeNumberLiterals(f, numbers));
    DexUtils.dexMethods(dexClass).forEach(m -> decodeNumberLiterals(m, numbers));
    return numbers;
  }

  public static Set<Long> decodeNumberLiterals(@Nonnull DexBackedMethod dexMethod) {
    Set<Long> numbers = new HashSet<>();
    decodeNumberLiterals(dexMethod, numbers);
    return numbers;
  }

  public static Object decodeFieldValue(@Nonnull DexBackedField dexField) {
    return decodeValue(dexField.getInitialValue());
  }

  private static ReferencePool decodeDexReferences(DexBackedDexFile dexFile,
                                                   ReferenceTypes types,
                                                   boolean resolve) {
    RefPoolBuffer buffer = new RefPoolBuffer(types);
    if (types.hasString()) dexFile.getStringReferences().forEach(buffer::add);
    if (types.hasTypeDes()) dexFile.getTypeReferences().forEach(buffer::add);
    if (types.hasField()) dexFile.getFieldSection().forEach(buffer::add);
    if (types.hasMethod()) dexFile.getMethodSection().forEach(buffer::add);
    return buffer.getPool(resolve);
  }

  private static ReferencePool decodeClassReferences(DexBackedClassDef dexClass,
                                                     ReferenceTypes types,
                                                     boolean resolve) {
    RefPoolBuffer buffer = new RefPoolBuffer(types);
    decodeClassFieldReferences(dexClass, types, buffer);
    getMethods(dexClass, types).forEach(m -> decodeMethodReferences(m, types, buffer));
    return buffer.getPool(resolve);
  }

  private static Iterable<DexBackedMethod> getMethods(DexBackedClassDef dexClass,
                                                      ReferenceTypes types) {
    switch (types.getScope()) {
      default: return Collections.emptyList();
      case ALL: return DexUtils.dexMethods(dexClass, types.synthEnabled());
      case DIRECT: return DexUtils.dexDirectMethods(dexClass, types.synthEnabled());
      case VIRTUAL: return DexUtils.dexVirtualMethods(dexClass, types.synthEnabled());
    }
  }

  private static void decodeClassFieldReferences(DexBackedClassDef dexClass,
                                                 ReferenceTypes types,
                                                 RefPoolBuffer buffer) {
    if (types.hasString()) {
      DexUtils.dexStaticFields(dexClass).forEach(field -> decodeFieldReferences(field, buffer));
    }
  }

  private static void decodeFieldReferences(DexBackedField dexField,
                                            RefPoolBuffer buffer) {
    EncodedValue value = dexField.getInitialValue();
    if (value != null && value.getValueType() == ValueType.STRING) {
      buffer.add(((StringEncodedValue) value).getValue());
    }
  }

  private static ReferencePool decodeMethodReferences(DexBackedMethod dexMethod,
                                                      ReferenceTypes types,
                                                      boolean resolve) {
    RefPoolBuffer buffer = new RefPoolBuffer(types);
    decodeMethodReferences(dexMethod, types, buffer);
    return buffer.getPool(resolve);
  }

  private static void decodeMethodReferences(DexBackedMethod dexMethod,
                                             ReferenceTypes types,
                                             RefPoolBuffer buffer) {
    MethodImplementation implementation = dexMethod.getImplementation();
    if (implementation == null || types.hasNone()) return;
    for (Instruction instruction : implementation.getInstructions()) {
      if (instruction instanceof ReferenceInstruction) {
        decodeReference(((ReferenceInstruction) instruction).getReference(), types, buffer);
        if (instruction instanceof DualReferenceInstruction) {
          decodeReference(((DualReferenceInstruction) instruction).getReference2(), types, buffer);
        }
      }
    }
  }

  private static void decodeReference(Reference reference,
                                      ReferenceTypes types,
                                      RefPoolBuffer buffer) {
    try {
      reference.validateReference();
      if (reference instanceof StringReference) {
        if (types.hasString()) buffer.add(((StringReference) reference));
      } else if (reference instanceof FieldReference) {
        if (types.hasField()) buffer.add(((FieldReference) reference));
      } else if (reference instanceof MethodReference) {
        if (types.hasMethod()) buffer.add(((MethodReference) reference));
      } else if (reference instanceof TypeReference) {
        if (types.hasTypeDes()) buffer.add(((TypeReference) reference));
      }
    } catch (Reference.InvalidReferenceException e) {
      DexLog.w(e.getMessage());
    }
  }

  private static void decodeNumberLiterals(DexBackedMethod dexMethod, Set<Long> numbers) {
    MethodImplementation implementation = dexMethod.getImplementation();
    if (implementation == null) return;
    for (Instruction instruction : implementation.getInstructions()) {
      if (instruction instanceof WideLiteralInstruction) {
        numbers.add(((WideLiteralInstruction) instruction).getWideLiteral());
      }
    }
  }

  private static void decodeNumberLiterals(DexBackedField dexField, Set<Long> numbers) {
    EncodedValue value = dexField.getInitialValue();
    if (!DexUtils.hasValue(value)) return;
    switch (value.getValueType()) {
      case ValueType.SHORT:
        numbers.add((long) ((ShortEncodedValue) value).getValue());
        break;
      case ValueType.INT:
        numbers.add((long) ((IntEncodedValue) value).getValue());
        break;
      case ValueType.LONG:
        numbers.add(((LongEncodedValue) value).getValue());
        break;
      case ValueType.FLOAT:
        numbers.add((long) Float.floatToRawIntBits(((FloatEncodedValue) value).getValue()));
        break;
      case ValueType.DOUBLE:
        numbers.add(Double.doubleToRawLongBits(((DoubleEncodedValue) value).getValue()));
        break;
    }
  }

  private static Object decodeValue(EncodedValue value) {
    if (!DexUtils.hasValue(value)) return null;
    switch (value.getValueType()) {
      case ValueType.CHAR:
        return ((CharEncodedValue) value).getValue();
      case ValueType.BYTE:
        return ((ByteEncodedValue) value).getValue();
      case ValueType.SHORT:
        return ((ShortEncodedValue) value).getValue();
      case ValueType.INT:
        return ((IntEncodedValue) value).getValue();
      case ValueType.LONG:
        return ((LongEncodedValue) value).getValue();
      case ValueType.FLOAT:
        return ((FloatEncodedValue) value).getValue();
      case ValueType.DOUBLE:
        return ((DoubleEncodedValue) value).getValue();
      case ValueType.BOOLEAN:
        return ((BooleanEncodedValue) value).getValue();
      case ValueType.STRING:
        return ((StringEncodedValue) value).getValue();
    }
    return null;
  }
}
