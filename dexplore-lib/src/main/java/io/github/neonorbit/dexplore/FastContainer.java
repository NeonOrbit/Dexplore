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

import io.github.neonorbit.dexplore.util.Internal;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.ZipDexContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Internal
final class FastContainer extends ZipDexContainer {
  private final boolean isApkFile;
  private final boolean rootDexOnly;
  private FastContainer(@Nonnull File zipFilePath,
                        @Nullable Opcodes opcodes,
                        boolean isApkFile, boolean rootDexOnly) {
    super(zipFilePath, opcodes);
    this.isApkFile = isApkFile;
    this.rootDexOnly = rootDexOnly;
  }

  @Nullable
  public static FastContainer load(@Nonnull File zipFilePath,
                                   @Nullable Opcodes opcodes,
                                   boolean rootDexOnly) {
    try (ZipFile zip = new ZipFile(zipFilePath)) {
      boolean isApkFile = rootDexOnly || zip.getEntry("AndroidManifest.xml") != null;
      return new FastContainer(zipFilePath, opcodes, isApkFile, rootDexOnly);
    } catch (IOException ignore) {
      return null;
    }
  }

  @Override
  protected boolean isDex(@Nonnull ZipFile zipFile,
                          @Nonnull ZipEntry zipEntry) throws IOException {
    final String name = zipEntry.getName();
    boolean filter = rootDexOnly ? name.startsWith("classes") && name.endsWith(".dex")
                                 : (!(isApkFile && (name.startsWith("r/") ||
                                                    name.startsWith("res/") ||
                                                    name.startsWith("lib/")
                                                   )
                                   ));
    return filter && super.isDex(zipFile, zipEntry);
  }
}
