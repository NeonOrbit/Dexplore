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

package io.github.neonorbit.dexplore

import com.beust.jcommander.JCommander
import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters
import io.github.neonorbit.dexplore.CommandLine.TITLE
import io.github.neonorbit.dexplore.CommandLine.VERSION
import io.github.neonorbit.dexplore.command.DecodeCommand
import io.github.neonorbit.dexplore.command.MapverCommand
import io.github.neonorbit.dexplore.command.SearchCommand
import io.github.neonorbit.dexplore.util.DexLog
import io.github.neonorbit.dexplore.util.DexLogger
import java.util.StringJoiner

internal class Commander(args: Array<String>) : JCommander() {
    @Parameter(names = ["-h", "--help"], help = true) private var help = false
    @Parameter(names = ["-v", "--verbose"], hidden = true) private var verbose = false

    private val allCommands = mapOf(
        Pair(SearchCommand.NAME, SearchCommand()),
        Pair(DecodeCommand.NAME, DecodeCommand()),
        Pair(MapverCommand.NAME, MapverCommand())
    )

    init {
        val empty = args.isEmpty()
        this.help = empty or help
        this.allCommands.forEach {
            val alias = it.key.first().toString()
            super.addCommand(it.key, it.value, alias)
        }
        super.addObject(this)
        super.setProgramName(TITLE)
        super.setExpandAtSign(false)
        super.parse(*args)
    }

    fun run() {
        val command = allCommands[parsedCommand]
        if (help || command?.validate() != true) {
            usage(help)
            return
        }
        if (verbose) {
            DexLog.enable()
            DexLog.setLogger(object : DexLogger() {
                override fun debug(msg: String) {
                    CommandUtils.print("D: $msg")
                }
                override fun warn(msg: String) {
                    CommandUtils.print("W: $msg")
                }
            })
        }
        command.apply()
    }

    private fun usage(help: Boolean) {
        if (help) {
            console.println("$TITLE v${VERSION}\n")
        }
        usage()
    }

    override fun usage() {
        val output = StringJoiner("\n")
        val program = programDisplayName
        val command = commands[parsedCommand]
        if (command == null) {
            output.add("Usage: $program <command> <files> [options]\n")
            output.add("Commands:")
            commands.values.forEach {
                output.add("  ${getName(it)}")
                output.add("      ${getDescription(it)}")
            }
            output.add("```")
            output.add("Print command details:  $program --help <command>")
            output.add("Examples: https://neonorbit.github.io/dexplore-wiki-cmd\n")
        } else {
            val origin = StringBuilder()
            command.usageFormatter.usage(origin)
            output.add("Usage: $program ${command.programName} <files> [options]")
            origin.toString().split("\n").filter { line ->
                line.isNotEmpty() && line.trim().let {
                    !it.startsWith("Usage: ") && !it.startsWith("Default: ")
                }
            }.forEachIndexed { i, line ->
                output.add((if (i != 1 && line.trim().first() == '-') "\n" else "") + line)
            }
            output.add("```")
            output.add("Examples: https://neonorbit.github.io/dexplore-wiki-cmd\n")
        }
        console.println(output.toString())
    }

    private fun getName(command: JCommander): String {
        return "${command.programName.first()}[${command.programName}]"
    }

    private fun getDescription(command: JCommander): String {
        return command.objects[0].javaClass.getAnnotation(Parameters::class.java).commandDescription
    }
}
