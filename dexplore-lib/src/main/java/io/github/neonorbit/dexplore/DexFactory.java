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
