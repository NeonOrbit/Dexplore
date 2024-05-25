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
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Internal
public class FilteredIterable<T> implements Iterable<T> {
  private final Iterable<T> source;
  private final Predicate<T> filter;

  public FilteredIterable(Iterable<T> source,
                          Predicate<T> filter) {
    this.source = source;
    this.filter = filter;
  }

  @Nonnull
  @Override
  public Iterator<T> iterator() {
    return new FilteredIterator();
  }

  @Override
  public void forEach(Consumer<? super T> action) {
    for (T item : source) {
      if (filter.test(item)) action.accept(item);
    }
  }

  private class FilteredIterator implements Iterator<T> {
    private T next = null;
    private boolean fetched, drained;
    private final Iterator<T> iterator = source.iterator();

    @Override
    public boolean hasNext() {
      if (!fetched) {
        fetched = true;
        next = advance();
      }
      return !drained;
    }

    @Override
    public T next() {
      if (hasNext()) {
        fetched = false;
        return next;
      }
      throw new NoSuchElementException();
    }

    private T advance() {
      while (iterator.hasNext()) {
        T n = iterator.next();
        if (filter.test(n)) return n;
      }
      drained = true;
      return null;
    }
  }
}
