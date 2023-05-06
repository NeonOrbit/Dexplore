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

package io.github.neonorbit.dexplore.decompiler

import jadx.api.JadxArgs
import jadx.api.JadxDecompiler
import jadx.api.JavaClass
import jadx.api.ResourceFile
import jadx.api.args.DeobfuscationMapFileMode.IGNORE
import jadx.api.impl.NoOpCodeCache
import jadx.api.plugins.input.data.ILoadResult
import jadx.core.dex.nodes.RootNode
import jadx.core.dex.visitors.rename.RenameVisitor
import java.io.Closeable

internal class DexDecompiler(private val loader: DexInputLoader,
                             private val disableCache: Boolean,
                             private val renameClasses: Boolean,
                             private val includeSource: Boolean,
                             private val includeResource: Boolean) : Closeable {
    private val args: JadxArgs = JadxArgs().apply {
        isShowInconsistentCode = true
        isInlineAnonymousClasses = false
        deobfuscationMapFileMode = IGNORE
        isSkipResources = !includeResource
        if (!renameClasses) renameFlags = setOf()
        if (disableCache) codeCache = NoOpCodeCache()
    }
    private val decompiler: JadxDecompiler = JadxDecompiler(args)

    fun init() {
        try {
            load()
        } catch (_: Throwable) {
            decompiler.apply {
                addCustomLoad(loader.load)
            }.load()
        }
    }

    private fun load() {
        JadxDecompiler::class.java.getDeclaredField("root").let {
            it.isAccessible = true
            RootNode(args).also { root ->
                it.set(decompiler, root)
            }
        }.let { root ->
            if (includeSource) {
                root.loadClasses(listOf(loader.load))
                root.initClassPath()
                root.runPreDecompileStage()
                root.initPasses()
                if (renameClasses) {
                    args.inputFiles.add(loader.file)
                    RenameVisitor().init(root)
                    args.inputFiles.clear()
                }
            } else if (includeResource) {
                root.loadClasses(resLoader())
                root.initClassPath()
            }
            if (includeResource) {
                args.inputFiles.add(loader.file)
                root.loadResources(decompiler.resources)
                args.inputFiles.clear()
            } else {
                initEmptyResource()
            }
        }
    }

    fun buildBatches(): List<List<JavaClass>> {
        return if (!includeSource) listOf()
        else decompiler.decompileScheduler.buildBatches(decompiler.classes)
    }

    fun getResources(): List<ResourceFile> {
        return if (!includeResource) listOf() else loader.filterResources(decompiler)
    }

    private fun initEmptyResource() {
        try {
            JadxDecompiler::class.java.getDeclaredField("resources").let {
                it.isAccessible = true
                it.set(decompiler, listOf<ResourceFile>())
            }
        } catch (_: Throwable) {}
    }

    private fun resLoader(): List<ILoadResult> {
        return DexInputLoader(
            loader.file, { entry ->
                val i = entry.lastIndexOf('/', entry.length - 3)
                entry[i + 1] == 'R' && (entry[i + 2] == ';' || entry[i + 2] == '$')
            }, null
        ).load.let { listOf(it) }
    }

    override fun close() {
        decompiler.close()
    }
}
