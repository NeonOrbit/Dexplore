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

package io.github.neonorbit.dexplore;

import io.github.neonorbit.dexplore.util.DexException;
import org.jf.dexlib2.DexFileFactory;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A factory class for loading dex files
 */
public final class DexFactory {
  /**
   * @param path file path
   * @return Dexplore instance
   * @throws DexException if something goes wrong
   * @throws FileNotFoundException if the given file does not exist
   * @throws UnsupportedFileException if the given file is not a valid dex file
   */
  @Nonnull
  public static Dexplore load(@Nonnull String path) {
    return load(path, DexOptions.getDefault());
  }

  @Nonnull
  public static Dexplore load(@Nonnull String path,
                              @Nonnull DexOptions options) {
    DexploreImpl dexplore;
    Objects.requireNonNull(path);
    Objects.requireNonNull(options);
    try {
      dexplore = new DexploreImpl(path, options);
    } catch (DexFileFactory.DexFileNotFoundException e) {
      throw new FileNotFoundException(e.getMessage());
    } catch (DexFileFactory.UnsupportedFileTypeException e) {
      throw new UnsupportedFileException(e.getMessage());
    }
    return dexplore;
  }

  public static class UnsupportedFileException extends RuntimeException {
    private UnsupportedFileException(String msg) {
      super(msg);
    }
  }

  public static class FileNotFoundException extends RuntimeException {
    private FileNotFoundException(String msg) {
      super(msg);
    }
  }
}
