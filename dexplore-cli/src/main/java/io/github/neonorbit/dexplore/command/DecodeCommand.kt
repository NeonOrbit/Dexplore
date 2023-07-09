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
import io.github.neonorbit.dexplore.DexFileDecoder
import io.github.neonorbit.dexplore.util.DexUtils
import java.io.File
import java.util.function.Predicate
import java.util.regex.Pattern

@Parameters(commandNames = [DecodeCommand.NAME], commandDescription = DecodeCommand.DESC)
internal class DecodeCommand : Command {
    companion object {
        const val NAME = "decode"
        const val DESC = "Decompile java, smali and resource files"
        private val VALID_DECODE_MODES = setOf("j", "s", "r")
    }

    @Parameter(description = "files")
    var files: List<String> = ArrayList()

    @Parameter(
            order = 1,
            names = ["-m", "--mode"],
            description = "Decode mode: j: java (default), s: smali, r: resources"
    )
    var decodeMode = "j"

    @Parameter(
            order = 2,
            variableArity = true,
            names = ["-cls", "--classes"],
            description = "Decompile a list of classes only (fully qualified name)"
    )
    var classes = ArrayList<String>()

    @Parameter(
        order = 3,
        variableArity = true,
        names = ["-cnm", "--cls-names"],
        description = "Decompile a list of classes by names (simple short name)"
    )
    var classNames = ArrayList<String>()

    @Parameter(
        order = 4,
        names = ["-reg", "--cls-regex"],
        description = "Filter classes with java regex pattern (against full name)"
    )
    var classRegex = ""

    @Parameter(
            order = 5,
            variableArity = true,
            names = ["-pkg", "--packages"],
            description = "Decompile a list of packages. Default: all"
    )
    var packages = ArrayList<String>()

    @Parameter(
        order = 6,
        variableArity = true,
        names = ["-res", "--resources"],
        description = "Resource types: color, values, drawable etc. Default: all"
    )
    var resources = ArrayList<String>()

    @Parameter(
            order = 7,
            names = ["-job", "--jobs"],
            description = "The number of threads to use. Default: [core-size]"
    )
    private var threadCount = CommandUtils.cores()

    @Parameter(
        order = 8,
        names = ["-dren", "--disable-rename"],
        description = "Disable class names renaming. Default: enabled"
    )
    private var disableRename = false

    @Parameter(
        order = 9,
        names = ["-dmem", "--disable-cache"],
        description = "Disable In-Memory cache. Default: enabled"
    )
    private var disableMemCache = false

    @Parameter(
        order = 10,
        names = ["-eps", "--enable-pause"],
        description = "Pause capability (with ENTER key). Default: disabled"
    )
    private var pauseSupport = false

    @Parameter(
            order = 11,
            names = ["-o", "--output"],
            description = "Output directory. Default: dexplore-out"
    )
    private var output = "dexplore-out"

    override fun apply() {
        DexFileDecoder(output, threadCount, pauseSupport).apply {
            srcFilter = buildSrcFilter()
            resFilter = buildResFilter()
            disableCache = disableMemCache
            renameClass = !disableRename
            decodeRes = 'r' in decodeMode
            decodeJava = 'j' in decodeMode
            decodeSmali = 's' in decodeMode
        }.let { decoder ->
            files.map { File(it) }.also {
                CommandUtils.checkFiles(it)
            }.forEach { file ->
                val msg = "File: ${file.name}"
                CommandUtils.print(msg)
                decoder.decode(file)
                CommandUtils.print()
            }
        }
    }

    private fun buildResFilter(): Predicate<String>? {
        return resources.takeIf { it.isNotEmpty() }?.map {
            "res/$it"
        }?.let { mapped ->
            Predicate { entry ->
                mapped.stream().anyMatch { entry.startsWith(it) }
            }
        }
    }

    private fun buildSrcFilter(): Predicate<String>? {
        val lPackages = packages.takeIfNoneEmpty()?.map { list ->
            "L${list.replace('.', '/')}/"
        }
        val lClasses = classes.takeIfNoneEmpty()?.let { list ->
            DexUtils.javaToDexTypeName(list).map { it.dropLast(1) }
        }
        val lClassNames = classNames.takeIfNoneEmpty()?.map {
            Regex("(?:^|.*[/\$])\\Q" + it.substringAfterLast('.') + "\\E[;\$].*")
        }
        val lClassRegex = classRegex.takeIf { it.isNotEmpty() }?.let {
            DexUtils.javaToDexPattern(Pattern.compile(classRegex))
        }
        return if (lPackages == null && lClasses == null && lClassNames == null && lClassRegex == null) {
            return null
        } else Predicate<String> { entry ->
            (lPackages != null && lPackages.any { entry.startsWith(it) }) ||
            (lClasses != null && matchClassesIncludingInner(lClasses, entry)) ||
            (lClassNames != null && lClassNames.any { entry.matches(it) }) ||
            (lClassRegex != null) && lClassRegex.matcher(entry).matches()
        }
    }

    private fun matchClassesIncludingInner(classes: List<String>, entry: String): Boolean {
        return classes.any { cls ->
            entry.length > cls.length && entry.startsWith(cls) && entry[cls.length].let {
                it == ';' || it == '$'
            }
        }
    }

    private fun List<String>.takeIfNoneEmpty(): List<String>? {
        return if (isNotEmpty() && all { it.isNotEmpty() }) this else null
    }

    override fun validate(): Boolean {
        if (files.isEmpty()) {
            CommandUtils.error("\n  Please provide input files\n")
            return false
        }
        if (decodeMode.any { it.toString() !in VALID_DECODE_MODES }) {
            CommandUtils.error("\n  Please enter correct decode mode\n")
            return false
        }
        if (classNames.any { '.' in it }) {
            CommandUtils.error("\n  [-cnm, --cls-names] Invalid class simple-names\n")
            return false
        }
        return true
    }
}
