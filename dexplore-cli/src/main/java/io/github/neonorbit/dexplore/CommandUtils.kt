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

import java.io.File
import java.io.FileNotFoundException

internal object CommandUtils {
    private val HEX by lazy {
        Regex("^[+-]?0x[0-9a-f]+$")
    }
    fun String.isHex() = matches(HEX)

    fun memoryUsage(): String {
        val maximum = Runtime.getRuntime().maxMemory()
        val used = runtime().totalMemory() - runtime().freeMemory()
        return String.format("Memory: [ %d%% | %.2fGB/%.2fGB ] ",
            (used * 100 / maximum), byteToGb(used), byteToGb(maximum)
        )
    }

    fun cores(): Int = runtime().availableProcessors()

    private fun runtime(): Runtime = Runtime.getRuntime()

    fun print(msg: String = "", ln: Boolean = true) {
        val newline = if (ln) "\n" else ""
        StdStreamHandler.directOut("$msg$newline")
    }

    fun error(msg: String) {
        StdStreamHandler.directErr(msg)
    }

    fun error(msg: String, t: Throwable) {
        error("$msg[${t.javaClass.simpleName}]: ${t.message}")
    }

    fun reprint(msg: String) {
        StdStreamHandler.directWrite("${backspaces(msg.length)}$msg")
    }

    private fun backspaces(len: Int): String {
        return "\b \b".repeat(len)
    }

    fun checkFiles(files: List<File>) {
        for (file in files) checkFile(file)
    }

    fun checkFile(file: File) {
        if (!file.isFile) throw FileNotFoundException("${file.name} file does not exist")
    }

    private fun byteToGb(byte: Long): Double = (byte.toDouble() / (1024 * 1024 * 1024))
}
