package io.github.neonorbit.dexplore;

import java.util.HashMap;

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
      return 31 * first.hashCode() + second.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj instanceof Key) {
        final Key another = (Key) obj;
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
