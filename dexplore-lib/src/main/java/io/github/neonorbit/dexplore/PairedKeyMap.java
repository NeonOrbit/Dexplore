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

import java.util.HashMap;
import java.util.Objects;

final class PairedKeyMap<T> {
  private static class Key {
    private final Object first;
    private final Object second;

    private Key(Object first, Object second) {
      this.first = first;
      this.second = second;
    }

    @Override
    public int hashCode() {
      return Objects.hash(first, second);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj instanceof Key) {
        Key another = (Key) obj;
        return this.first.equals(another.first) &&
               this.second.equals(another.second);
      }
      return false;
    }
  }

  private final HashMap<Key, T> internal = new HashMap<>();

  public void put(Object first, Object second, T value) {
    internal.put(new Key(first, second), value);
  }

  public T get(Object first, Object second) {
    return internal.get(new Key(first, second));
  }
}
