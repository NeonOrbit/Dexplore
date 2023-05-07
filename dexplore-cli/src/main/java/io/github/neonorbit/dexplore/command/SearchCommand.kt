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
import io.github.neonorbit.dexplore.cliutil.CmdAdvancedQuery
import io.github.neonorbit.dexplore.cliutil.CmdQuery
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
            names = ["-rt", "--ref-type"],
            description = "Reference types: a: all, s: string, t: type, f: field, m: method"
    )
    var type = ""

    @Parameter(
            order = 6,
            variableArity = true,
            names = ["-ref", "--references"],
            description = "References: string, type, field or method names"
    )
    var references: List<String> = ArrayList()

    @Parameter(
        order = 7,
        variableArity = true,
        names = ["-sig", "--signatures"],
        description = "Same as --references except that it compares with signatures"
    )
    var signatures: List<String> = ArrayList()

    @Parameter(
        order = 8,
        variableArity = true,
        names = ["-src", "--sources"],
        description = "Provide a list of source names to match against (eg: 'Cache.java')"
    )
    var sources = ArrayList<String>()

    @Parameter(
        order = 9,
        variableArity = true,
        names = ["-num", "--numbers"],
        description = "Provide a list of numbers to match against (eg: 123 124.1f 121.1d)"
    )
    var numbers = ArrayList<String>()

    @Parameter(
            order = 10,
            names = ["-l", "--limit"],
            description = "Limit maximum results. Default: -1 (no limit)"
    )
    private var maximum = -1

    @Parameter(
            order = 11,
            names = ["-pool", "--print-pool"],
            description = "Print ReferencePool: a: all, s: string, t: type, f: field, m: method"
    )
    var printPool = ""

    @Parameter(
            order = 12,
            names = ["-gen", "--gen-sources"],
            description = "Generate java and smali source files from search results"
    )
    private var generate = false

    @Parameter(
            order = 13,
            names = ["-o", "--output"],
            description = "Output directory. Default: dexplore-out"
    )
    private var output = "dexplore-out"

    @Parameter(
        order = 14,
        names = ["-cdv", "--class-advanced"],
        description = "'m:public+..., s:superclass, i:interface+..., a:annotation+...'"
    )
    private var cAdvanced = ""

    @Parameter(
        order = 15,
        names = ["-mdv", "--method-advanced"],
        description = "'m:public+..., n:name+..., p:param+..., r:return, a:annot+..., z:size'"
    )
    private var mAdvanced = ""

    override fun apply() {
        val decoder = DexFileDecoder(output).apply {
            flatOutput = true
            decodeJava = true
            decodeSmali = true
        }
        val engine = DexSearchEngine(searchMode).apply {
            setMaximum(maximum)
            setDetails(printPool)
            init(
                CmdQuery(packages, classes, clsNames, type, references, signatures, sources, numbers),
                CmdAdvancedQuery.parse(cAdvanced), CmdAdvancedQuery.parse(mAdvanced)
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

    override fun validate(): Boolean {
        if (output.isEmpty()) {
            CommandUtils.error("\n  Invalid output directory name\n")
            return false
        }
        if (files.isEmpty()) {
            CommandUtils.error("\n  Please provide input files\n")
            return false
        }
        if (searchMode !in VALID_SEARCH_MODES && searchMode.length != 1) {
            CommandUtils.error("\n  Please enter correct search mode\n")
            return false
        }
        if (classes.isEmpty() && clsNames.isEmpty() &&
            sources.isEmpty() && numbers.isEmpty() && type.isEmpty() &&
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
            if (references.isEmpty() && signatures.isEmpty()) {
                CommandUtils.error("\n  Please provide references\n")
                return false
            }
        } else if (references.isNotEmpty() || signatures.isNotEmpty()) {
            CommandUtils.error("\n  Please provide reference types\n")
            return false
        }
        if (printPool.isNotEmpty() && printPool.any { it.toString() !in VALID_REFERENCE_TYPES }) {
            CommandUtils.error("\n  Please enter correct details types\n")
            return false
        }
        return true
    }
}
