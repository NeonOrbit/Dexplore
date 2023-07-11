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

object CmdAdvSpec {
    object KEY {
        const val FLAGS = 'f'
        const val SUPER = 's'
        const val IFACES = 'i'
        const val METHODS = 'm'
        const val PARAMS = 'p'
        const val PSIZE = 'q'
        const val RETURN = 'r'
    }

    const val QUERY_DIVIDER = ','
    const val KEY_EXTRACTOR = ':'
    const val VALUE_DIVIDER = '+'

    fun isClassQuery(key: Char) = key in C_QUERIES
    fun isMethodQuery(key: Char) = key in M_QUERIES
    fun isEmptyValueAllowed(key: Char) = key in E_ALLOWED

    private val E_ALLOWED = charArrayOf(KEY.IFACES, KEY.PARAMS)
    private val C_QUERIES = charArrayOf(KEY.FLAGS, KEY.SUPER, KEY.IFACES)
    private val M_QUERIES = charArrayOf(KEY.FLAGS, KEY.METHODS, KEY.PARAMS, KEY.PSIZE, KEY.RETURN)

    const val CLASS_QUERY_FORMAT = "'f:public+final+..., s:superclass, i:interface1+interface2+..."
    const val METHOD_QUERY_FORMAT = "'f:public+..., m:methodName+..., p:param1+..., r:return, q:paramSize'"
}
