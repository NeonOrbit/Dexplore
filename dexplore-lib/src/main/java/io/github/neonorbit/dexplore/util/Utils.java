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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Internal
public final class Utils {
  private static final Map<String, Class<?>> PRIMITIVE = new HashMap<>();

  static {
    PRIMITIVE.put("void", Void.TYPE);
    PRIMITIVE.put("byte", Byte.TYPE);
    PRIMITIVE.put("char", Character.TYPE);
    PRIMITIVE.put("short", Short.TYPE);
    PRIMITIVE.put("int", Integer.TYPE);
    PRIMITIVE.put("long", Long.TYPE);
    PRIMITIVE.put("float", Float.TYPE);
    PRIMITIVE.put("double", Double.TYPE);
    PRIMITIVE.put("boolean", Boolean.TYPE);
  }

  @Nonnull
  public static Class<?> loadClass(@Nonnull ClassLoader classLoader,
                                   @Nonnull String name) throws ClassNotFoundException {
    if (PRIMITIVE.containsKey(name)) return PRIMITIVE.get(name);
    return Class.forName(name, true, classLoader);
  }

  public static boolean hasItem(@Nullable Collection<?> c) {
    return c != null && !c.isEmpty();
  }

  public static boolean isSingle(@Nullable Collection<?> c) {
    return c != null && c.size() == 1;
  }

  @Nullable
  public static <T> T findFirst(@Nonnull Collection<T> c) {
    return c.isEmpty() ? null : c.iterator().next();
  }

  public static <T> Set<T> optimizedSet(@Nonnull Collection<T> c) {
    if (c.isEmpty()) return Collections.emptySet();
    if (c.size() == 1) return Collections.singleton(c.iterator().next());
    Set<T> set = (c instanceof Set) ? (Set<T>) c : new HashSet<>(c);
    return Collections.unmodifiableSet(set);
  }

  public static <T> List<T> optimizedList(@Nonnull Collection<T> c) {
    if (c.isEmpty()) return Collections.emptyList();
    if (c.size() == 1) return Collections.singletonList(c.iterator().next());
    List<T> list = (c instanceof List) ? (List<T>) c : new ArrayList<>(c);
    return Collections.unmodifiableList(list);
  }

  public static void checkNotNull(Object... o) {
    if (Arrays.stream(Objects.requireNonNull(o)).anyMatch(Objects::isNull)) {
      throw new NullPointerException();
    }
  }

  public static <T> List<T> nonNullList(T[] a) {
    checkNotNull((Object[]) a);
    return Collections.unmodifiableList(Arrays.asList(a));
  }

  public static boolean isEquals(@Nonnull List<?> list, @Nonnull Object[] array) {
    if (list.isEmpty()) return array.length == 0;
    if (list.size() != array.length) return false;
    for (int i = 0; i < array.length; i++) {
      if (!list.get(i).equals(array[i])) return false;
    }
    return true;
  }

  public static <T extends Comparable<? super T>> int compare(T[] a, T[] b) {
    if (a == b) return 0;
    int length = Math.min(a.length, b.length);
    for (int i = 0; i < length; i++) {
      if (a[i] == b[i]) continue;
      int compare = a[i].compareTo(b[i]);
      if (compare != 0) return compare;
    }
    return a.length - b.length;
  }

  @SuppressWarnings("unchecked")
  public static List<String> toStringList(List<?> list) {
    if (list.isEmpty()) return Collections.emptyList();
    if (list.get(0) instanceof String) return (List<String>) list;
    return list.stream().map(Object::toString).collect(Collectors.toList());
  }

  public static boolean isValidName(List<String> names) {
    return names.stream().allMatch(Utils::isValidName);
  }

  public static boolean isValidName(String name) {
    if (name == null) return false;
    for (String s : name.split("\\.", -1)) {
      if (s.isEmpty())
        return false;
      for (int i = 0; i < s.length(); i++) {
        if (!Character.isJavaIdentifierPart(s.charAt(i)))
          return false;
      }
    }
    return true;
  }
}
