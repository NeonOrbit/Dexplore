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

import io.github.neonorbit.dexplore.exception.DexException;
import io.github.neonorbit.dexplore.exception.FileNotFoundException;
import io.github.neonorbit.dexplore.exception.UnsupportedFileException;
import org.jf.dexlib2.DexFileFactory;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * A factory class for loading dex files.
 * <p>
 * Supported types: apk, zip, dex, odex, oat.
 * <p>
 * <b>Note:</b>
 * Thread safety is ensured for {@linkplain Dexplore dexplore} instances created via this class.
 * <p>
 * Available methods:
 * <ul>
 *   <li>{@link #load(String) load(path)}</li>
 *   <li>{@link #load(String, DexOptions) load(path, options)}</li>
 *   <li>{@link #load(byte[]) load(buffer)}</li>
 *   <li>{@link #load(byte[], DexOptions) load(buffer, options)}</li>
 * </ul>
 *
 * @author NeonOrbit
 * @since 1.0.0
 */
public final class DexFactory {
  /**
   * Loads a file containing one or more dex files.
   * <p><b>Note:</b> The returned instance is thread-safe.</p>
   *
   * @param path the path of the file to open
   * @return A {@code Dexplore} for the given file
   * @throws DexException if failed to load the file
   * @throws FileNotFoundException if the given file does not exist
   * @throws UnsupportedFileException if the given file is not a valid dex file
   * @see #load(String, DexOptions) Dexplore.load(path, options)
   */
  @Nonnull
  public static Dexplore load(@Nonnull String path) {
    return load(path, DexOptions.getDefault());
  }

  /**
   * Loads a file containing one or more dex files.
   * <p><b>Note:</b> The returned instance is thread-safe.</p>
   *
   * @param path the path of the file to open
   * @param options a set of options to apply
   * @return A {@code Dexplore} for the given file
   * @throws DexException if failed to load the file
   * @throws FileNotFoundException if the given file does not exist
   * @throws UnsupportedFileException if the given file is not a valid dex file
   * @see #load(String) Dexplore.load(path)
   */
  @Nonnull
  public static Dexplore load(@Nonnull String path,
                              @Nonnull DexOptions options) {
    try {
      return new DexploreImpl(
              Objects.requireNonNull(path), Objects.requireNonNull(options)
      );
    } catch (DexFileFactory.DexFileNotFoundException e) {
      throw new FileNotFoundException(e.getMessage());
    } catch (DexFileFactory.UnsupportedFileTypeException e) {
      throw new UnsupportedFileException(e.getMessage());
    }
  }

  /**
   * Loads a dex or odex file from memory.
   * <p><b>Note:</b> The returned instance is thread-safe.</p>
   *
   * @param buffer a byte array containing the dex file
   * @return A {@code Dexplore} instance for the given file
   * @throws UnsupportedFileException if the given file is not a valid dex file
   * @see #load(byte[], DexOptions) Dexplore.load(buffer, options)
   */
  @Nonnull
  public static Dexplore load(@Nonnull byte[] buffer) {
    return load(buffer, DexOptions.getDefault());
  }

  /**
   * Loads a dex or odex file from memory.
   * <p><b>Note:</b> The returned instance is thread-safe.</p>
   *
   * @param buffer a byte array containing the dex file
   * @param options a set of dex options to apply
   * @return A {@code Dexplore} instance for the given file
   * @throws UnsupportedFileException if the given file is not a valid dex file
   * @see #load(byte[]) Dexplore.load(buffer)
   */
  @Nonnull
  public static Dexplore load(@Nonnull byte[] buffer,
                              @Nonnull DexOptions options) {
    return new DexploreImpl(
            Objects.requireNonNull(buffer), Objects.requireNonNull(options)
    );
  }
}
