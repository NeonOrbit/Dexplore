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

package io.github.neonorbit.dexplore.command

import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters
import io.github.neonorbit.dexplore.CommandUtils
import io.github.neonorbit.dexplore.DexSearchEngine
import io.github.neonorbit.dexplore.DexFileDecoder
import io.github.neonorbit.dexplore.cliutil.CmdAdvSpec
import io.github.neonorbit.dexplore.cliutil.CmdAdvancedQuery
import io.github.neonorbit.dexplore.cliutil.CmdQuery
import io.github.neonorbit.dexplore.cliutil.CmdQuery.Companion.buildRefTypes
import io.github.neonorbit.dexplore.util.DexUtils
import java.io.File
import java.util.function.Predicate

@Parameters(commandNames = [SearchCommand.NAME], commandDescription = SearchCommand.DESC)
internal class SearchCommand : Command {
    companion object {
        const val NAME = "search"
        const val DESC = "Search classes and methods"
        private val VALID_SEARCH_MODES = setOf("c", "m")
        private val VALID_REFERENCE_TYPES = setOf("a", "s", "t", "f", "m")
    }

    @Parameter(description = "files")
    var files: List<String> = ArrayList()

    @Parameter(
        order = 1,
        names = ["-m", "--mode"],
        description = "Search mode: c: find class (default), m: find method"
    )
    var searchMode = "c"

    @Parameter(
        order = 2,
        variableArity = true,
        names = ["-pkg", "--packages"],
        description = "Search from a list of packages only. Default: all"
    )
    var packages = ArrayList<String>()

    @Parameter(
        order = 3,
        variableArity = true,
        names = ["-cls", "--classes"],
        description = "Search a list of classes only (fully qualified name)"
    )
    var classes = ArrayList<String>()

    @Parameter(
        order = 4,
        variableArity = true,
        names = ["-cnm", "--cls-names"],
        description = "Search a list of classes by names (simple short name)"
    )
    var clsNames = ArrayList<String>()

    @Parameter(
        order = 5,
        names = ["-clx", "--cls-regex"],
        description = "Filter classes with java regex (checks against the full name)"
    )
    var clsRegex = ""

    @Parameter(
        order = 6,
        names = ["-rt", "--ref-type"],
        description = "Reference types: a: all, s: string, t: type, f: field, m: method"
    )
    var type = ""

    @Parameter(
        order = 7,
        variableArity = true,
        names = ["-ref", "--references"],
        description = "References: string, type, field or method names"
    )
    var references: List<String> = ArrayList()

    @Parameter(
        order = 8,
        names = ["-rfx", "--ref-regex"],
        description = "A java regex that matches against the reference pools"
    )
    var refRegex = ""

    @Parameter(
        order = 9,
        variableArity = true,
        names = ["-sig", "--signatures"],
        description = "Same as --references except that it compares with signatures"
    )
    var signatures: List<String> = ArrayList()

    @Parameter(
        order = 10,
        variableArity = true,
        names = ["-src", "--sources"],
        description = "Provide a list of source names to match against (eg: 'Cache.java')"
    )
    var sources = ArrayList<String>()

    @Parameter(
        order = 11,
        variableArity = true,
        names = ["-num", "--numbers"],
        description = "Provide a list of numbers to match against (eg: 123 124.1f 121.1d)"
    )
    var numbers = ArrayList<String>()

    @Parameter(
        order = 12,
        variableArity = true,
        names = ["-res", "--res-name"],
        description = "Match against resource names: 'com.app.R' 'string:res_name' 'color:..'"
    )
    var resNames = ArrayList<String>()

    @Parameter(
        order = 13,
        variableArity = true,
        names = ["-ann", "--annot-type"],
        description = "Match based on contained annotations. (eg: 'java.lang.Deprecated')"
    )
    var annotTypes = ArrayList<String>()

    @Parameter(
        order = 14,
        variableArity = true,
        names = ["-anv", "--annot-value"],
        description = "Match based on contained annotation values (values of elements)"
    )
    var annotValues = ArrayList<String>()

    @Parameter(
        order = 15,
        names = ["-syn", "--synthetic"],
        description = "Enable synthetic items. Default: disabled"
    )
    private var synthetic = false

    @Parameter(
        order = 16,
        names = ["-l", "--limit"],
        description = "Limit maximum results. Default: -1 (no limit)"
    )
    private var maximum = -1

    @Parameter(
        order = 17,
        names = ["-pool", "--print-pool"],
        description = "Print ReferencePool: a: all, s: string, t: type, f: field, m: method"
    )
    var printPool = ""

    @Parameter(
        order = 18,
        names = ["-gen", "--gen-sources"],
        description = "Generate java and smali source files from search results"
    )
    private var generate = false

    @Parameter(
        order = 19,
        names = ["-o", "--output"],
        description = "Output directory. Default: dexplore-out"
    )
    private var output = "dexplore-out"

    @Parameter(
        order = 20,
        names = ["-cdv", "--class-advanced"],
        description = CmdAdvSpec.CLASS_QUERY_FORMAT
    )
    private var cAdvanced = ""

    @Parameter(
        order = 21,
        names = ["-mdv", "--method-advanced"],
        description = CmdAdvSpec.METHOD_QUERY_FORMAT
    )
    private var mAdvanced = ""

    override fun apply() {
        val isClass = searchMode == "c"
        val decoder = DexFileDecoder(output).apply {
            flatOutput = true
            decodeJava = true
            decodeSmali = true
        }
        val engine = DexSearchEngine(isClass).apply {
            setMaximum(maximum)
            setDetails(buildRefTypes(printPool))
            setResourceNames(parseResNames(resNames))
            init(
                CmdQuery(
                    packages, classes, clsNames, clsRegex, type, references, refRegex,
                    signatures, sources, numbers, annotTypes, annotValues, synthetic
                ),
                CmdAdvancedQuery.parse(isClass, cAdvanced), CmdAdvancedQuery.parse(isClass, mAdvanced)
            )
        }
        files.map { File(it) }.also {
            CommandUtils.checkFiles(it)
        }.forEach { file ->
            CommandUtils.print("File: ${file.name}")
            engine.search(file.path).takeIf {
                it.isNotEmpty() && generate
            }?.let {
                val cList = DexUtils.javaToDexTypeName(it)
                decoder.srcFilter = Predicate { e -> e in cList }
                CommandUtils.print("Generating sources...")
                decoder.decode(file)
            }
            CommandUtils.print()
        }
    }

    private fun parseResNames(resNames: ArrayList<String>): List<String> {
        if (resNames.isEmpty()) return listOf()
        val resClass = resNames.first()
        return resNames.drop(1).map {
            val (type, name) = it.split(':')
            val inner = type.ifEmpty { "" }
            "$resClass\$$inner.$name"
        }
    }

    override fun validate(): Boolean {
        if (output.isEmpty()) {
            CommandUtils.error("\n  Invalid output directory name\n")
            return false
        }
        if (files.isEmpty()) {
            CommandUtils.error("\n  Please provide input files\n")
            return false
        }
        if (searchMode.length != 1 || searchMode !in VALID_SEARCH_MODES) {
            CommandUtils.error("\n  Please enter correct search mode\n")
            return false
        }
        if (listOf(classes, clsNames, sources, resNames, numbers, annotTypes, annotValues).all {
            it.isEmpty()
        } && listOf(type, clsRegex).all { it.isEmpty() } &&
            (searchMode != "c" || cAdvanced.isEmpty()) &&
            (searchMode != "m" || mAdvanced.isEmpty())) {
            CommandUtils.error("\n  Please provide a search query\n")
            return false
        }
        if (classes.isNotEmpty() && clsNames.isNotEmpty()) {
            CommandUtils.error(
                "\n (-cls, --classes) cannot be used together with (-cnm, --cls-names)\n"
            )
            return false
        }
        if (type.isNotEmpty()) {
            if (type.any { it.toString() !in VALID_REFERENCE_TYPES }) {
                CommandUtils.error("\n  Please enter correct reference types\n")
                return false
            }
            if (references.isEmpty() && signatures.isEmpty() && refRegex.isEmpty()) {
                CommandUtils.error("\n  Please provide references [-ref, --references]\n")
                return false
            }
        } else if (references.isNotEmpty() || signatures.isNotEmpty() || refRegex.isNotEmpty()) {
            CommandUtils.error("\n  Please provide reference types [-rt, -ref-type]\n")
            return false
        }
        if (resNames.isNotEmpty()) {
            if (resNames.size < 2) {
                CommandUtils.error("\n  [-res, --res-name] Please provide " +
                        (if (resNames[0] == "R" || resNames[0].endsWith(".R") ) "resource names"
                        else "the R class as first value") + '\n'
                )
                return false
            }
            if (resNames.drop(1).any { it.contains('.') || it.split(':').size != 2 }) {
                CommandUtils.error("\n  [-res, --res-name] Please enter correct resource names\n")
                return false
            }
        }
        if (printPool.isNotEmpty() && printPool.any { it.toString() !in VALID_REFERENCE_TYPES }) {
            CommandUtils.error("\n  [-pool, --print-pool] Please enter correct pool types\n")
            return false
        }
        return true
    }
}
