package io.github.neonorbit.dexplore.filter;

import io.github.neonorbit.dexplore.LazyDecoder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

abstract class BaseFilter<T> {
  protected final boolean pass;
  protected final ReferenceTypes types;
  protected final ReferenceFilter filter;

  protected BaseFilter(Builder<?,?> builder) {
    if (builder.types == null ||
        builder.filter == null ||
        builder.types.hasNone()) {
      this.pass = true;
      this.types = ReferenceTypes.none();
      this.filter = referencePool -> true;
    } else {
      this.pass = false;
      this.types = builder.types;
      this.filter = builder.filter;
    }
  }

  public boolean verify(@Nonnull T dexItem, @Nonnull LazyDecoder<T> decoder) {
    return pass || filter.verify(decoder.decode(dexItem, types));
  }

  protected static abstract class Builder<B extends Builder<B,?>,
                                          T extends BaseFilter<?>> {
    private ReferenceTypes types;
    private ReferenceFilter filter;

    protected Builder() {}
    
    protected Builder(T instance) {
      this.types = instance.types;
      this.filter = instance.filter;
    }

    protected abstract B getThis();

    public abstract T build();

    public B setReferenceTypes(@Nullable ReferenceTypes types) {
      this.types = types;
      return getThis();
    }

    public B setReferenceFilter(@Nullable ReferenceFilter filter) {
      this.filter = filter;
      return getThis();
    }
  }
}
