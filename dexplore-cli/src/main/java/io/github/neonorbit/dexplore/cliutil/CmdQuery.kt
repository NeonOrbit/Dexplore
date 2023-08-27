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

import io.github.neonorbit.dexplore.CommandUtils.isHex
import io.github.neonorbit.dexplore.filter.ReferenceTypes
import java.util.regex.Pattern

class CmdQuery(
    val packages: List<String>,
    val classes: List<String>,
    val classNames: List<String>,
    classRegex: String,
    referenceTypes: String,
    val references: List<String>,
    referenceRegex: String,
    val signatures: List<String>,
    val sources: List<String>,
    numbers: List<String>,
    val annotTypes: List<String>,
    val annotValues: List<String>,
    val enableSynthetic: Boolean
) {
    val refTypes = buildRefTypes(referenceTypes)
    val numbers: List<Number> = parseNumbers(numbers)
    val clsPattern = if (classRegex.isEmpty()) null else Pattern.compile(classRegex)
    val refPattern = if (referenceRegex.isEmpty()) null else Pattern.compile(referenceRegex, Pattern.DOTALL)

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
        fun buildRefTypes(types: String): ReferenceTypes {
            return if ("a" in types) ReferenceTypes.all() else {
                val builder = ReferenceTypes.builder()
                if ("s" in types) builder.addString()
                if ("t" in types) builder.addTypeDes()
                if ("f" in types) builder.addFieldWithDetails()
                if ("m" in types) builder.addMethodWithDetails()
                builder.build()
            }
        }
    }
}
