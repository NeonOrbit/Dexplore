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

import io.github.neonorbit.dexplore.ReferencePool;
import io.github.neonorbit.dexplore.util.Utils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Contains information about a class found in a dex file.
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public final class ClassData implements Comparable<ClassData> {
  /** The fully-qualified class name. */
  @Nonnull public final String clazz;

  private ReferencePool referencePool;
  private Map<String, MethodData> methods;

  ClassData(@Nonnull String clazz) {
    this.clazz = clazz;
  }

  void setMethods(Map<String, MethodData> methods) {
    this.methods = methods;
  }

  void setReferencePool(ReferencePool referencePool) {
    this.referencePool = referencePool;
  }

  MethodData getMethod(String signature) {
    return Objects.requireNonNull(methods).get(signature);
  }

  /**
   * Returns a set containing {@code MethodData} objects
   * representing all the declared methods of the class.
   * <p>The returned set is unmodifiable.</p>
   *
   * @return a set of objects representing the declared methods of the class
   */
  @Nonnull
  public Collection<MethodData> getMethods() {
    if (methods == null) {
      methods = Collections.emptyMap();
    }
    return methods.values();
  }

  /**
   * Returns the {@code ReferencePool} of the class.
   * <p>It contains all the {@link io.github.neonorbit.dexplore.reference references}
   * present in the class.</p>
   *
   * @return the {@code ReferencePool} of the class
   */
  @Nonnull
  public ReferencePool getReferencePool() {
    if (referencePool == null) {
      referencePool = ReferencePool.emptyPool();
    }
    return referencePool;
  }

  @Nullable
  public Class<?> loadClass(@Nonnull ClassLoader classLoader) {
    try {
      return classLoader.loadClass(clazz);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  /**
   * Serializes the object into a string.
   * <p> Includes: {@link #clazz} </p>
   *
   * @return the serialized string
   */
  @Nonnull
  public String serialize() {
    return this.clazz;
  }

  /**
   * De-serializes the given string.
   *
   * @param serialized the string to be de-serialized
   * @return the de-serialized object
   * @throws IllegalArgumentException if the given string is not serializable
   */
  @Nonnull
  public static ClassData deserialize(@Nonnull String serialized) {
    if (Utils.isValidName(serialized)) {
      return new ClassData(serialized);
    }
    throw new IllegalArgumentException();
  }

  @Override
  public int compareTo(@Nonnull ClassData o) {
    return this.clazz.compareTo(o.clazz);
  }

  @Override
  public int hashCode() {
    return clazz.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return (this == obj) || (obj instanceof ClassData) &&
           (this.clazz.equals(((ClassData) obj).clazz));
  }

  @Override
  public String toString() {
    return clazz;
  }
}
