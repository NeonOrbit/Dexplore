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

import io.github.neonorbit.dexplore.cliutil.CmdAdvSpec.KEY
import io.github.neonorbit.dexplore.cliutil.CmdAdvSpec.KEY_EXTRACTOR
import io.github.neonorbit.dexplore.cliutil.CmdAdvSpec.QUERY_DIVIDER
import io.github.neonorbit.dexplore.cliutil.CmdAdvSpec.VALUE_DIVIDER
import java.lang.reflect.Modifier

internal class CmdAdvancedQuery {
    var modifiers: Int = -1
        private set
    var superClass: String? = null
        private set
    var interfaces: List<String>? = null
        private set
    var paramSize: Int = -1
        private set
    var methodNames: List<String> = listOf()
        private set
    var methodParams: List<String>? = null
        private set
    var methodReturn: String? = null
        private set

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

        private fun toModifier(name: String) = MODIFIERS[name] ?: throwIllegal("modifier: $name")

        fun parse(isClass: Boolean, raw: String): CmdAdvancedQuery {
            val advanced = CmdAdvancedQuery()
            if (raw.isEmpty()) return advanced
            raw.split(QUERY_DIVIDER).forEach { query ->
                val (key, args) = divideQuery(query)
                val values = args.split(VALUE_DIVIDER).sanitize(key)
                checkQueryKeyValidity(key, isClass)
                when (key) {
                    KEY.FLAGS -> {
                        advanced.modifiers = values.map {
                            toModifier(it.uppercase())
                        }.reduce { acc, value -> acc or value }
                    }
                    KEY.SUPER -> advanced.superClass = values.first()
                    KEY.IFACES -> advanced.interfaces = values
                    KEY.METHODS -> advanced.methodNames = values
                    KEY.PARAMS -> advanced.methodParams = values
                    KEY.PSIZE -> advanced.paramSize = values.first().toInt()
                    KEY.RETURN -> advanced.methodReturn = values.first()
                    else -> throwIllegal("advanced query key: $key")
                }
            }
            return advanced
        }

        private fun divideQuery(query: String): Pair<Char, String> {
            val del = KEY_EXTRACTOR
            if (del !in query) throwIllegal("advanced query: $query")
            return Pair(query.substringBefore(del).lastOrNull() ?: ' ', query.substringAfter(del)).also {
                if (it.first.isWhitespace()) throwIllegal("key in advanced query: $query")
            }
        }

        private fun List<String>.sanitize(key: Char): List<String> {
            return this.map { it.trim() }.filter { it.isNotEmpty() }.toList().also {
                if (!CmdAdvSpec.isEmptyValueAllowed(key) && it.isEmpty()) throwIllegal("value for key: $key")
            }
        }

        private fun checkQueryKeyValidity(key: Char, isClass: Boolean) {
            if (isClass && !CmdAdvSpec.isClassQuery(key)) throwIllegal("query key for class: $key")
            if (!isClass && !CmdAdvSpec.isMethodQuery(key)) throwIllegal("query key for method: $key")
        }

        private fun throwIllegal(msg: String): Nothing = throw IllegalArgumentException("Invalid $msg")
    }
}
