/*
 * Copyright (C) 2023 NeonOrbit
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
import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;

@Internal
public final class ShallowList<E> extends AbstractList<E> {
  private final Collection<E> internal;
  private Iterator<E> iterator;
  private int cursor = -1;

  public ShallowList(Collection<E> source) {
    this.internal = source;
  }

  public static <E> ShallowList<E> of(Collection<E> source) {
    return new ShallowList<>(source);
  }

  @Override
  public E get(int index) {
    updateIterator(index);
    return iterator.next();
  }

  @Override
  public int size() {
    return internal.size();
  }

  @Nonnull
  @Override
  public Iterator<E> iterator() {
    return internal.iterator();
  }

  @Override
  public boolean contains(Object o) {
    return internal.contains(o);
  }

  private void updateIterator(int index) {
    checkIndex(index);
    if (cursor < 0 || cursor >= size() || cursor > index) {
      cursor = 0;
      iterator = internal.iterator();
    }
    while (cursor++ < index) iterator.next();
  }

  private void checkIndex(int index) {
    if (index < 0 || index >= size())
      throw new IndexOutOfBoundsException("Index out of range: " + index);
  }
}
