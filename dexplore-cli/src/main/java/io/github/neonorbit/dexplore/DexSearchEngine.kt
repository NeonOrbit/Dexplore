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

import io.github.neonorbit.dexplore.exception.DexException
import io.github.neonorbit.dexplore.filter.ClassFilter
import io.github.neonorbit.dexplore.filter.DexFilter
import io.github.neonorbit.dexplore.filter.MethodFilter
import io.github.neonorbit.dexplore.filter.ReferenceFilter
import io.github.neonorbit.dexplore.filter.ReferenceTypes
import io.github.neonorbit.dexplore.result.DexItemData
import java.util.StringJoiner
import java.util.function.Function

internal class DexSearchEngine(mode: String) {
    private var initialized = false
    private var maximumResult = 0
    private val titleResult: String
    private val titleSearch: String
    private val isClassMode: Boolean
    private lateinit var dexFilter: DexFilter
    private lateinit var classFilter: ClassFilter
    private lateinit var methodFilter: MethodFilter
    private lateinit var detailsType: ReferenceTypes

    init {
        isClassMode = mode == "c"
        titleResult = if (isClassMode) "Class" else "Method"
        titleSearch = if (isClassMode) "classes" else "methods"
    }

    fun setMaximum(maximum: Int) {
        maximumResult = maximum
    }

    fun setDetails(details: String) {
        detailsType = buildRefTypes(details)
    }

    fun init(classes: List<String>,
             sources: List<String>,
             refTypes: String,
             references: List<String>,
             signatures: List<String>) {
        checkEngineState(false)
        val types = buildRefTypes(refTypes)
        val filter = if (types.hasNone()) null else ReferenceFilter { pool ->
            val result = references.stream().allMatch { pool.contains(it) }
            result and signatures.stream().allMatch { pool.toString().contains(it) }
        }
        dexFilter = DexFilter.MATCH_ALL
        classFilter = ClassFilter
                .builder()
                .setClasses(*classes.toTypedArray())
                .setSourceNames(*sources.toTypedArray())
                .setReferenceTypes(types)
                .setReferenceFilter(filter)
                .build()
        methodFilter = if (isClassMode) MethodFilter.MATCH_ALL else MethodFilter
                .builder()
                .setReferenceTypes(types)
                .setReferenceFilter(filter)
                .build()
        initialized = true
    }

    fun search(file: String): Set<String> {
        checkEngineState(true)
        try {
            return dexQuery(file)
        } catch (e: DexException) {
            CommandUtils.error("Failed", e)
        }
        return setOf()
    }

    private fun dexQuery(file: String): Set<String> {
        val results = HashSet<String>()
        CommandUtils.print("Searching $titleSearch...")
        val handler = Function { result: DexItemData ->
            if (results.isEmpty()) {
                CommandUtils.print("Result:")
            }
            results.add(result.clazz)
            CommandUtils.print("+ $titleResult: $result")
            printReferencePool(result)
            maximumResult > 0 && results.size >= maximumResult
        }
        with(DexFactory.load(file)) {
            if (isClassMode) {
                onClassResult(dexFilter, classFilter) { handler.apply(it) }
            } else {
                onMethodResult(dexFilter, classFilter, methodFilter) { handler.apply(it) }
            }
        }
        if (results.isEmpty()) CommandUtils.print("Result:  [Not Found]")
        return results
    }

    private fun printReferencePool(data: DexItemData) {
        if (detailsType.hasNone()) return
        val pool = data.referencePool
        val joiner = StringJoiner("\n   ")
        joiner.add("- ReferencePool: ")
        if (detailsType.hasString()) {
            joiner.add("String References: ")
            if (pool.stringSection.isEmpty()) joiner.add("  [EMPTY]")
            else pool.stringSection.forEach { joiner.add("  $it") }
        }
        if (detailsType.hasTypeDes()) {
            joiner.add("Type References: ")
            if (pool.typeSection.isEmpty()) joiner.add("  [EMPTY]")
            else pool.typeSection.forEach { joiner.add("  $it") }
        }
        if (detailsType.hasField()) {
            joiner.add("Field References: ")
            if (pool.fieldSection.isEmpty()) joiner.add("  [EMPTY]")
            else pool.fieldSection.forEach { joiner.add("  $it") }
        }
        if (detailsType.hasMethod()) {
            joiner.add("Method References: ")
            if (pool.methodSection.isEmpty()) joiner.add("  [EMPTY]")
            else pool.methodSection.forEach { joiner.add("  $it") }
        }
        CommandUtils.print(joiner.toString())
    }

    private fun buildRefTypes(types: String): ReferenceTypes {
        return if ("a" in types) {
            ReferenceTypes.all()
        } else {
            val builder = ReferenceTypes.builder()
            if ("s" in types) builder.addString()
            if ("t" in types) builder.addTypeDes()
            if ("f" in types) builder.addFieldWithDetails()
            if ("m" in types) builder.addMethodWithDetails()
            builder.build()
        }
    }

    private fun checkEngineState(state: Boolean) {
        check(state == initialized) {
            if (state) "Engine is not initialized"
            else "Engine is already initialized"
        }
    }
}
