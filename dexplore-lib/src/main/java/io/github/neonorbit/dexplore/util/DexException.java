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

public class DexException extends RuntimeException {
  public DexException(String message) {
    super(message);
  }

  public DexException(String message, Throwable cause) {
    super(getMessage(message, cause), getCause(cause));
    if (cause instanceof DexException) {
      setStackTrace(cause.getStackTrace());
    }
  }

  private static Throwable getCause(Throwable cause) {
    return (cause instanceof DexException) ? cause.getCause() : cause;
  }

  private static String getMessage(String message, Throwable cause) {
    return (cause instanceof DexException) ? cause.getMessage() : message;
  }
}
