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

import jadx.api.JadxDecompiler
import jadx.api.ResourceFile
import jadx.api.ResourceType.ARSC
import jadx.api.plugins.input.data.IClassData
import jadx.plugins.input.dex.DexFileLoader
import jadx.plugins.input.dex.DexInputOptions
import jadx.plugins.input.dex.DexLoadResult
import jadx.plugins.input.dex.DexReader
import java.io.File
import java.util.function.Consumer
import java.util.function.Predicate

internal class DexInputLoader(val file: File,
                              private val srcFilter: Predicate<String>?,
                              private val resFilter: Predicate<String>?) {
    val load: Loader by lazy {
        Loader(DexFileLoader(DexInputOptions()).collectDexFiles(listOf(file.toPath())))
    }

    inner class Loader(dexReaders: List<DexReader>) : DexLoadResult(dexReaders, null) {
        override fun visitClasses(consumer: Consumer<IClassData>) {
            super.visitClasses {
                if (srcFilter?.test(it.type) != false) consumer.accept(it)
            }
        }
    }

    fun filterResources(decompiler: JadxDecompiler): List<ResourceFile> {
        return decompiler.resources.filter {
            val name = if (it.type == ARSC) "res/values" else it.deobfName
            name.equals(MANIFEST) || (name.startsWith("res/") && resFilter?.test(name) != false)
        }
    }

    companion object {
        private const val MANIFEST = "AndroidManifest.xml"
    }
}
