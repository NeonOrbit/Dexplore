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
    val refTypes: String,
    val references: List<String>,
    val signatures: List<String>,
    val sources: List<String>,
    numbers: List<String>
) {
    val numbers = parseNumbers(numbers)
    companion object {
        private fun parseNumbers(numbers: List<String>): List<Long> {
            return numbers.map { n ->
                when (n.last()) {
                    'd' -> n.toDouble().toBits()
                    'f' -> n.toFloat().toBits().toLong()
                    else -> if ('.' in n) n.toDouble().toBits() else n.toLong()
                }
            }.toList()
        }
    }
}
