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
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.ValueType;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.instruction.DualReferenceInstruction;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.Reference;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.iface.reference.TypeReference;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.iface.value.StringEncodedValue;
import org.jf.dexlib2.immutable.reference.ImmutableStringReference;

import javax.annotation.Nonnull;
import java.util.Collections;

public final class DexDecoder {
  private final boolean cache;
  private final PairedKeyMap<ReferencePool> dexCache;
  private final PairedKeyMap<ReferencePool> classCache;

  DexDecoder(DexOptions options) {
    this.cache = options.enableCache;
    this.dexCache = new PairedKeyMap<>();
    this.classCache = new PairedKeyMap<>();
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
  public static ReferencePool decodeFully(@Nonnull DexBackedMethod dexMethod) {
    return decodeMethodReferences(dexMethod, ReferenceTypes.all(), true);
  }

  private static ReferencePool decodeDexReferences(DexBackedDexFile dexFile,
                                                   ReferenceTypes types,
                                                   boolean resolve) {
    final RefPoolRBuffer buffer = new RefPoolRBuffer(types);
    if (types.hasString()) dexFile.getStringReferences().forEach(buffer::add);
    if (types.hasField()) dexFile.getFieldSection().forEach(buffer::add);
    if (types.hasMethod()) dexFile.getMethodSection().forEach(buffer::add);
    if (types.hasTypeDes()) dexFile.getTypeReferences().forEach(buffer::add);
    return buffer.getPool(resolve);
  }

  private static ReferencePool decodeClassReferences(DexBackedClassDef dexClass,
                                                     ReferenceTypes types,
                                                     boolean resolve) {
    final RefPoolRBuffer buffer = new RefPoolRBuffer(types);
    decodeClassFieldReferences(dexClass, types, buffer);
    getMethods(dexClass, types).forEach(dexMethod -> {
      if (!AccessFlags.SYNTHETIC.isSet(dexMethod.accessFlags)) {
        decodeMethodReferences(dexMethod, types, buffer);
      }
    });
    return buffer.getPool(resolve);
  }

  private static Iterable<? extends DexBackedMethod> getMethods(DexBackedClassDef dexClass,
                                                                ReferenceTypes types) {
    return (ReferenceTypes.Scope.ALL == types.getScope() ? dexClass.getMethods() :
            ReferenceTypes.Scope.DIRECT == types.getScope() ? dexClass.getDirectMethods() :
            ReferenceTypes.Scope.VIRTUAL == types.getScope() ? dexClass.getVirtualMethods() :
                                                                    Collections.emptyList());
  }

  private static void decodeClassFieldReferences(DexBackedClassDef dexClass,
                                                 ReferenceTypes types,
                                                 RefPoolRBuffer pool) {
    if (!types.hasString()) return;
    dexClass.getStaticFields().forEach(field -> {
      EncodedValue value = field.getInitialValue();
      if (value != null && value.getValueType() == ValueType.STRING) {
        pool.add(new ImmutableStringReference(((StringEncodedValue)value).getValue()));
      }
    });
  }

  private static ReferencePool decodeMethodReferences(DexBackedMethod dexMethod,
                                                      ReferenceTypes types,
                                                      boolean resolve) {
    RefPoolRBuffer buffer = new RefPoolRBuffer(types);
    decodeMethodReferences(dexMethod, types, buffer);
    return buffer.getPool(resolve);
  }

  private static void decodeMethodReferences(DexBackedMethod dexMethod,
                                             ReferenceTypes types,
                                             RefPoolRBuffer pool) {
    MethodImplementation implementation = dexMethod.getImplementation();
    if (implementation == null || types.hasNone()) return;
    for (Instruction instruction : implementation.getInstructions()) {
      if (instruction instanceof ReferenceInstruction) {
        decodeReference(((ReferenceInstruction) instruction).getReference(), types, pool);
        if (instruction instanceof DualReferenceInstruction) {
          decodeReference(((DualReferenceInstruction) instruction).getReference2(), types, pool);
        }
      }
    }
  }

  private static void decodeReference(Reference reference,
                                      ReferenceTypes types,
                                      RefPoolRBuffer pool) {
    try {
      if (reference instanceof StringReference) {
        if (types.hasString()) pool.add(((StringReference) reference));
      } else if (reference instanceof FieldReference) {
        if (types.hasField()) pool.add(((FieldReference) reference));
      } else if (reference instanceof MethodReference) {
        if (types.hasMethod()) pool.add(((MethodReference) reference));
      } else if (reference instanceof TypeReference) {
        if (types.hasTypeDes()) pool.add(((TypeReference) reference));
      }
    } catch (Exception ignore) { }
  }
}
