/*
 * Copyright (C) 2023 NeonOrbit
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

package io.github.neonorbit.dexplore.cliutil

class CmdQuery(
    val packages: List<String>,
    val classes: List<String>,
    val classNames: List<String>,
    val refTypes: String,
    val references: List<String>,
    val signatures: List<String>,
    val sources: List<String>,
    numbers: List<String>
) {
    val numbers: List<Number> = parseNumbers(numbers)
    companion object {
        private fun parseNumbers(numbers: List<String>): List<Number> {
            return numbers.map {
                val num = it.lowercase().removeSuffix("l")
                when {
                    num.isHex() -> num.replace("0x", "").toLong(16)
                    num.last() == 'f' -> num.toFloat()
                    num.last() == 'd' || '.' in num -> num.toDouble()
                    else -> num.toLong()
                }
            }.toList()
        }
        private val HEX = Regex("^[+-]?0x[0-9a-f]+$")
        private fun String.isHex() = matches(HEX)
    }
}
