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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class Utils {
  public static boolean isSingle(@Nullable Collection<?> c) {
    return c != null && c.size() == 1;
  }

  @Nullable
  public static <T> T findFirst(@Nonnull Collection<T> c) {
    return c.isEmpty() ? null : c.iterator().next();
  }

  public static <T> Set<T> optimizedSet(@Nonnull Collection<T> c) {
    if (c.isEmpty()) return Collections.emptySet();
    if (c.size() == 1) return Collections.singleton(findFirst(c));
    return Collections.unmodifiableSet(new HashSet<>(c));
  }

  public static <T> List<T> optimizedList(@Nonnull Collection<T> c) {
    if (c.isEmpty()) return Collections.emptyList();
    if (c.size() == 1) return Collections.singletonList(findFirst(c));
    return Collections.unmodifiableList(new ArrayList<>(c));
  }

  public static <T> List<T> nonNullList(T[] a) {
    Objects.requireNonNull(a);
    if (Arrays.stream(a).anyMatch(Objects::isNull))
      throw new NullPointerException();
    return Arrays.asList(a);
  }

  public static boolean isValidName(List<String> names) {
    return names.stream().allMatch(Utils::isValidName);
  }

  public static boolean isValidName(String name) {
    if (name == null) return false;
    for (String s : name.split("\\.", -1)) {
      if (s.isEmpty())
        return false;
      if (!Character.isJavaIdentifierStart(s.charAt(0)))
        return false;
      for (int i = 1; i < s.length(); i++) {
        if (!Character.isJavaIdentifierPart(s.charAt(i)))
          return false;
      }
    }
    return true;
  }
}
