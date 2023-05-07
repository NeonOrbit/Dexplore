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

import java.lang.reflect.Modifier

internal class CmdAdvancedQuery {
    var modifiers: Int = -1
    var superClass: String? = null
    var interfaces: List<String>? = null
    var methodNames: List<String> = listOf()
    var methodParams: List<String>? = null
    var methodReturn: String? = null
    var annotations: List<String> = listOf()
    var methodParamSize: Int = -1

    companion object {
        private val MODIFIERS = mapOf(
            Pair("PUBLIC", Modifier.PUBLIC),
            Pair("PROTECTED", Modifier.PROTECTED),
            Pair("PRIVATE", Modifier.PRIVATE),
            Pair("ABSTRACT", Modifier.ABSTRACT),
            Pair("STATIC", Modifier.STATIC),
            Pair("FINAL", Modifier.FINAL),
            Pair("STRICT", Modifier.STRICT),
            Pair("NATIVE", Modifier.NATIVE),
            Pair("SYNCHRONIZED", Modifier.SYNCHRONIZED)
        )

        private fun toModifier(name: String) = MODIFIERS[name] ?: throw Exception("Invalid modifier")

        fun parse(raw: String): CmdAdvancedQuery {
            val advanced = CmdAdvancedQuery()
            if (raw.isEmpty()) return advanced
            raw.split(',').forEach { query ->
                val key = query.substringBefore(':', "").last()
                val values = query.substringAfter(':', "").split('+').map { it.trim() }.toList()
                when (key) {
                    'm' -> {
                        advanced.modifiers = values.map {
                            toModifier(it.uppercase())
                        }.reduce { acc, value -> acc or value }
                    }
                    's' -> advanced.superClass = values.first()
                    'i' -> advanced.interfaces = values
                    'n' -> advanced.methodNames = values
                    'p' -> advanced.methodParams = values
                    'r' -> advanced.methodReturn = values.first()
                    'a' -> advanced.annotations = values
                    'z' -> advanced.methodParamSize = values.first().toInt()
                }
            }
            return advanced
        }
    }
}
