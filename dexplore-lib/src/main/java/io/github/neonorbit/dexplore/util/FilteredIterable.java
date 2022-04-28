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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

@Internal
public class FilteredIterable<T> implements Iterable<T> {
  private final Iterator<T> iterator;
  private final Predicate<T> filter;

  public FilteredIterable(Iterator<T> iterator,
                          Predicate<T> filter) {
    this.iterator = iterator;
    this.filter = filter;
  }

  private T advance() {
    while (iterator.hasNext()) {
      T n = iterator.next();
      if (filter.test(n)) return n;
    }
    return null;
  }

  @Override
  public Iterator<T> iterator() {
    return new Iterator<T>() {
      private T next = advance();

      @Override
      public boolean hasNext() {
        return next != null;
      }

      @Override
      public T next() {
        if (hasNext()) {
          T current = next;
          next = advance();
          return current;
        } else {
          throw new NoSuchElementException();
        }
      }
    };
  }
}
