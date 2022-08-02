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

package io.github.neonorbit.dexplore

import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream

internal object StdStreamHandler {
    private val STDOUT = System.out
    private val STDERR = System.err

    private val DUMMY = PrintStream(
        object : OutputStream() {
            override fun write(b: Int) {}
        }, true
    )

    fun directOut(msg: String) {
        STDOUT.print(msg)
    }

    fun directErr(msg: String) {
        STDERR.println(msg)
    }

    fun directWrite(msg: String) {
        try {
            synchronized(STDOUT) {
                STDOUT.write(msg.toByteArray())
            }
        } catch (ignore: IOException) { }
    }

    @Suppress("unused")
    fun disableOut() {
        System.setOut(DUMMY)
    }

    fun disableErr() {
        System.setErr(DUMMY)
    }

    fun restore() {
        if (System.out !== STDOUT) {
            System.setOut(STDOUT)
        }
        if (System.err !== STDERR) {
            System.setErr(STDERR)
        }
    }
}
