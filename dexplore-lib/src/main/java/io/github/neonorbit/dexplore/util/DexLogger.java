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

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DexLogger {
  private static final Logger LOGGER;

  static {
    LOGGER = Logger.getLogger(DexLogger.class.getName());
  }

  public void debug(String msg) {
    LOGGER.log(Level.INFO, msg);
  }

  public void warn(String msg) {
    LOGGER.log(Level.WARNING, msg);
  }

  public void error(String msg, Throwable t) {
    LOGGER.log(Level.SEVERE, msg, t);
  }
}
