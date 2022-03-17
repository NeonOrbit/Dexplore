package io.github.neonorbit.dexplore;

import io.github.neonorbit.dexplore.filter.ReferenceTypes;

public interface LazyDecoder<T> {
  ReferencePool decode(T dexItem, ReferenceTypes types);
}