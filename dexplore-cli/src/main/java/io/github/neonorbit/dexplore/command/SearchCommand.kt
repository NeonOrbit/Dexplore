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
            names = ["-cls", "--classes"],
            description = "Search a list of classes (full name)"
    )
    var classes = ArrayList<String>()

    @Parameter(
            order = 3,
            names = ["-rt", "--ref-type"],
            description = "Reference types: a: all, s: string, t: type, f: field, m: method"
    )
    var type = ""

    @Parameter(
            order = 4,
            variableArity = true,
            names = ["-ref", "--references"],
            description = "References: string, type, field or method names"
    )
    var references: List<String> = ArrayList()

    @Parameter(
        order = 5,
        variableArity = true,
        names = ["-sig", "--signatures"],
        description = "Same as --references except that it compares with signatures"
    )
    var signatures: List<String> = ArrayList()

    @Parameter(
            order = 6,
            names = ["-l", "--limit"],
            description = "Limit maximum results. Default: -1 (no limit)"
    )
    private var maximum = -1

    @Parameter(
            order = 7,
            names = ["-pool", "--print-pool"],
            description = "Print ReferencePool: a: all, s: string, t: type, f: field, m: method"
    )
    var printPool = ""

    @Parameter(
            order = 8,
            names = ["-gen", "--gen-sources"],
            description = "Generate java and smali source files from search results"
    )
    private var generate = false

    @Parameter(
            order = 9,
            names = ["-o", "--output"],
            description = "Output directory. Default: dexplore-out"
    )
    private var output = "dexplore-out"

    @Parameter(
        order = 10,
        hidden = true,
        names = ["-advance", "--advanced"],
        description = "\"m:public,final+a:annotation+s:superclass+i:interface1,interface2\""
    )
    private var advanced = ""

    override fun apply() {
        val decoder = DexFileDecoder(output).apply {
            flatOutput = true
            decodeJava = true
            decodeSmali = true
        }
        val engine = DexSearchEngine(searchMode).apply {
            setMaximum(maximum)
            setDetails(printPool)
            init(classes, type, references, signatures)
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
        if (advanced.isNotEmpty()) {
            TODO("--advanced option is not yet implemented")
        }
        if (output.isEmpty()) {
            CommandUtils.error("\n  Invalid output directory name\n")
            return false
        }
        if (files.isEmpty()) {
            CommandUtils.error("\n  Please provide input files\n")
            return false
        }
        if (searchMode !in VALID_SEARCH_MODES) {
            CommandUtils.error("\n  Please enter correct search mode\n")
            return false
        }
        if (classes.isEmpty() && type.isEmpty()) {
            CommandUtils.error("\n  Please provide a search query\n")
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
