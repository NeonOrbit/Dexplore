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

import io.github.neonorbit.dexplore.ConsoleMonitor.Handler
import io.github.neonorbit.dexplore.decompiler.DexInputLoader
import io.github.neonorbit.dexplore.decompiler.DexDecompiler
import io.github.neonorbit.dexplore.task.TaskHandler
import jadx.api.JavaClass
import jadx.core.dex.visitors.SaveCode
import jadx.core.utils.exceptions.JadxRuntimeException
import jadx.core.xmlgen.ResourcesSaver
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Predicate

internal class DexFileDecoder(private val output: String,
                              private val threadCount: Int = 2,
                              private val pauseSupport: Boolean = false) {
    var flatOutput: Boolean = false
    var decodeJava: Boolean = false
    var decodeSmali: Boolean = false
    var decodeRes: Boolean = false
    var renameClass: Boolean = false
    var disableCache: Boolean = false
    var noComments: Boolean = false
    var srcFilter: Predicate<String>? = null
    var resFilter: Predicate<String>? = null

    private val taskHandler: TaskHandler<Any> by lazy {
        if (pauseSupport) setupPauseSupport()
        TaskHandler(threadCount, pauseSupport)
    }

    private fun buildDecompiler(file: File): DexDecompiler {
        return DexDecompiler(
            loader = DexInputLoader(file, srcFilter, resFilter),
            disableCache = disableCache,
            renameClasses = renameClass,
            noCodeComments = noComments,
            includeSource = (decodeJava || decodeSmali),
            includeResource = decodeRes
        )
    }

    fun decode(file: File) {
        val dir = getDir(file)?: return
        CommandUtils.print("Preparing...")
        try {
            StdStreamHandler.disableErr()
            buildDecompiler(file).use { decompiler ->
                decompiler.init()
                decompiler.buildBatches().forEach { batch ->
                    taskHandler.dispatch {
                        batch.forEach { writeSourceFile(dir, it) }
                    }
                }
                decompiler.getResources().forEach { resource ->
                    taskHandler.dispatch {
                        ResourcesSaver(dir, resource).run()
                    }
                }
                if (!taskHandler.hasTask()) {
                    CommandUtils.print("Nothing to save.", false); return
                }
                taskHandler.awaitCompletion(200) { cur, total ->
                    CommandUtils.reprint(">> Saving... ${progress(cur, total)}")
                }
            }
        } catch (e: JadxRuntimeException) {
            CommandUtils.error("\nFailed", e)
        } finally {
            CommandUtils.print()
            StdStreamHandler.restore()
        }
    }

    private fun writeSourceFile(dir: File, entry: JavaClass) {
        val files = getValidFiles(dir, entry)
        if (decodeJava) {
            val java: String = try {
                entry.code
            } catch (e: JadxRuntimeException) {
                writeErrorList(dir, entry)
                e.toString()
            }
            if (java.isEmpty()) return
            SaveCode.save(java, files.first)
        }
        if (decodeSmali) {
            SaveCode.save(entry.smali, files.second)
        }
    }

    private fun writeErrorList(dir: File, entry: JavaClass) {
        try {
            FileWriter(File(dir, "_failed_classes"), true).use {
                synchronized(this) {
                    it.appendLine("- ${entry.fullName} (${entry.rawName})")
                }
            }
        } catch (ignore: IOException) {
        } catch (ignore: java.lang.UnsupportedOperationException) {}
    }

    private fun getValidFiles(dir: File, entry: JavaClass): Pair<File, File> {
        val path = if (flatOutput) entry.name else entry.classNode.classInfo.aliasFullPath
        var valid: String; var i = 0; var java: File; var smali: File
        do {
            valid = path + if (++i == 1) "" else "~$i"
            java = File(dir, getValidPath(valid, "java"))
            smali = File(dir, getValidPath(valid, "smali"))
        } while (java.exists() || smali.exists())
        return Pair(java, smali)
    }

    private fun getValidPath(name: String, type: String): String {
        return if (flatOutput) "$name.$type" else "$type${File.separatorChar}$name.$type"
    }

    private fun progress(cur: Int, total: Int): String {
        val percent = "[${(cur * 100 / total)}%]"
        return "$percent${" ".repeat(7 - percent.length)}"
    }

    private fun setupPauseSupport() {
        ConsoleMonitor.register(object : Handler {
            val paused = AtomicBoolean()
            override fun handle() {
                if (paused.get()) {
                    CommandUtils.print("Resumed...\n")
                    taskHandler.resume()
                    paused.set(false)
                } else {
                    paused.set(true)
                    taskHandler.pause()
                    CommandUtils.print("\nPaused...\n${
                        CommandUtils.memoryUsage()
                    }", false)
                }
            }
        })
        ConsoleMonitor.init()
    }

    private fun getDir(file: File): File? {
        CommandUtils.checkFile(file)
        var merge = false; val name = file.name
        val dir = File(output, "${name}_sources")
        if (dir.exists()) {
            CommandUtils.error("!! Output directory exists: ${dir.path}")
            ConsoleProvider.use {
                val line = it.readLine(">> Overwrite? [y/n]${
                    if (flatOutput) " or Merge? [m]" else ""
                }: ")
                if (line.equals("y", ignoreCase = true)) {
                    CommandUtils.print("Cleaning...")
                    if (!dir.deleteRecursively()) {
                        CommandUtils.error("!! Failed to overwrite: ${dir.path}")
                    }
                } else if (flatOutput && line.equals("m", ignoreCase = true)) {
                    merge = true
                }
            }
        }
        return if (merge || (!dir.exists() && dir.mkdirs())) dir else run {
            CommandUtils.error("!!--> Skipping: $name")
            null
        }
    }
}
