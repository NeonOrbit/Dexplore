package io.github.neonorbit.dexplore.filter;

import io.github.neonorbit.dexplore.ReferencePool;

import javax.annotation.Nonnull;
import java.util.Arrays;

public interface ReferenceFilter {
  boolean verify(ReferencePool pool);

  default ReferenceFilter and(@Nonnull ReferenceFilter other) {
    return pool -> verify(pool) && other.verify(pool);
  }

  default ReferenceFilter or(@Nonnull ReferenceFilter other) {
    return pool -> verify(pool) || other.verify(pool);
  }

  static ReferenceFilter contains(@Nonnull String value) {
    return pool -> pool.contains(value);
  }

  static ReferenceFilter containsAny(@Nonnull String... value) {
    return pool -> Arrays.stream(value).anyMatch(pool::contains);
  }

  static ReferenceFilter containsAll(@Nonnull String... value) {
    return pool -> Arrays.stream(value).allMatch(pool::contains);
  }

  static ReferenceFilter stringsContain(@Nonnull String value) {
    return pool -> pool.stringsContain(value);
  }

  static ReferenceFilter fieldsContain(@Nonnull String value) {
    return pool -> pool.fieldsContain(value);
  }

  static ReferenceFilter methodsContain(@Nonnull String value) {
    return pool -> pool.methodsContain(value);
  }

  static ReferenceFilter typesContain(@Nonnull String value) {
    return pool -> pool.typesContain(value);
  }
}
