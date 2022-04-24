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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

final class RefPoolRCache<T> {
  private final Map<Key, Entry> internal = new HashMap<>();
  private final ReferenceQueue<ReferencePool> queue = new ReferenceQueue<>();

  private static Key createKey(Object item, ReferenceTypes types) {
    return new Key(item.toString(), types);
  }

  public void put(T item, ReferenceTypes types, ReferencePool value) {
    Key key = createKey(item, types);
    internal.put(key, new Entry(key, value, queue));
  }

  public ReferencePool get(T item, ReferenceTypes types) {
    cleanStaleEntries();
    Key key = createKey(item, types);
    Entry entry = internal.get(key);
    if (entry != null) {
      ReferencePool value = entry.get();
      if (value != null) {
        return value;
      } else {
        internal.remove(key);
      }
    }
    return null;
  }

  private void cleanStaleEntries() {
    for (Object o; (o = queue.poll()) != null;) {
      internal.remove(((Entry) o).key);
    }
  }

  private static class Entry extends SoftReference<ReferencePool> {
    private final Key key;
    private Entry(Key key, ReferencePool value, ReferenceQueue<ReferencePool> queue) {
      super(value, queue);
      this.key = key;
    }
  }

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
}
