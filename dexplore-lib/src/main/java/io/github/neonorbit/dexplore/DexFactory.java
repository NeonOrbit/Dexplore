package io.github.neonorbit.dexplore;

import org.jf.dexlib2.DexFileFactory;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A factory class for loading dex files
 */
public final class DexFactory {
  @Nonnull
  public static Dexplore load(@Nonnull String path) {
    return new DexploreImpl(Objects.requireNonNull(path));
  }

  @Nonnull
  public static Dexplore load(@Nonnull String path,
                              @Nonnull DexOptions options) {
    return new DexploreImpl(Objects.requireNonNull(path),
                            Objects.requireNonNull(options));
  }
}
