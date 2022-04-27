package io.github.neonorbit.dexplore.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

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
