package io.github.neonorbit.dexplore.reference;

public interface DexReferenceData {
  /**
   * Checks whether any items of this {@code Object} match the specified string
   *
   * @param value The string to compare against
   * @return {@code true} if this {@code Object} contains the specified string
   */
  boolean contains(String value);
}
