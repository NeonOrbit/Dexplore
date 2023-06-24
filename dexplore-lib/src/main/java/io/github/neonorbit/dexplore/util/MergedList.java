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
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * A wrapper class representing a merged view of multiple lists.
 * <p>
 *   The source list can either be a list of lists
 *   or a list of objects, each of which can be mapped to a {@code List<E>}
 * </p>
 * @param <T> object type
 * @param <E> element type
 */
@Internal
public final class MergedList<T, E> extends AbstractList<E> {
  private final List<T> holder;
  private final Function<T, List<E>> mapper;
  private final int size;

  private MergedList(List<T> list, Function<T, List<E>> mapper) {
    this.holder = list;
    this.mapper = mapper;
    this.size = list.stream().mapToInt(t -> mapper.apply(t).size()).sum();
  }

  /**
   * @param list a list of objects, each of which can be mapped to a {@code List<E>}.
   * @param mapper A function that transforms each object into a {@code List<E>}.
   * @return a merged view of all the containing lists
   */
  public static <T, E> List<E> merge(List<T> list, Function<T, List<E>> mapper) {
    return new MergedList<>(list, mapper);
  }

  /**
   * @param lists a list of lists
   * @return a merged view of all the specified lists
   */
  public static <E> List<E> merge(List<List<E>> lists) {
    return new MergedList<>(lists, list -> list);
  }

  @Override
  public E get(int index) {
    checkIndex(index);
    for (T t : holder) {
      List<E> list = mapper.apply(t);
      if (index < list.size())
        return list.get(index);
      index -= list.size();
    }
    throw invalidIndex(index);
  }

  @Override
  public int size() {
    return size;
  }

  @Nonnull
  @Override
  public Iterator<E> iterator() {
    return new Iterator<E>() {
      private Iterator<E> it;
      private final Iterator<T> t = holder.iterator();

      @Override
      public boolean hasNext() {
        while (it == null || !it.hasNext()) {
          if (!t.hasNext()) return false;
          it = mapper.apply(t.next()).iterator();
        }
        return true;
      }

      @Override
      public E next() {
        return it.next();
      }
    };
  }

  private void checkIndex(int index) {
    if (index < 0 || index >= size) throw invalidIndex(index);
  }

  private static RuntimeException invalidIndex(int index) {
    return new IndexOutOfBoundsException("Index out of range: " + index);
  }
}
