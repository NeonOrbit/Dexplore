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

import io.github.neonorbit.dexplore.cliutil.CmdAdvancedQuery
import io.github.neonorbit.dexplore.cliutil.CmdQuery
import io.github.neonorbit.dexplore.exception.DexException
import io.github.neonorbit.dexplore.filter.ClassFilter
import io.github.neonorbit.dexplore.filter.DexFilter
import io.github.neonorbit.dexplore.filter.MethodFilter
import io.github.neonorbit.dexplore.filter.ReferenceFilter
import io.github.neonorbit.dexplore.filter.ReferenceTypes
import io.github.neonorbit.dexplore.result.ClassData
import io.github.neonorbit.dexplore.result.DexItemData
import io.github.neonorbit.dexplore.util.DexHelper
import java.util.StringJoiner
import java.util.function.Function
import java.util.regex.Pattern

internal class DexSearchEngine(private val classMode: Boolean) {
    private var initialized = false
    private var maximumResult = 0
    private var patternExists = false
    private var resourceNames = listOf<String>()
    private lateinit var dexFilter: DexFilter
    private lateinit var classFilter: ClassFilter
    private lateinit var methodFilter: MethodFilter
    private lateinit var detailsType: ReferenceTypes
    private lateinit var numberLiterals: List<Number>
    private val titleResult = if (classMode) "Class" else "Method"
    private val titleSearch = if (classMode) "classes" else "methods"

    fun setMaximum(maximum: Int) {
        this.maximumResult = maximum
    }

    fun setDetails(types: ReferenceTypes) {
        this.detailsType = types
    }

    fun setResourceNames(resNames: List<String>) {
        this.resourceNames = resNames
    }

    fun init(
        cmdQuery: CmdQuery,
        classAdvanced: CmdAdvancedQuery,
        methodAdvanced: CmdAdvancedQuery
    ) {
        checkEngineState(false)
        this.numberLiterals = cmdQuery.numbers
        this.patternExists = cmdQuery.classPattern != null
        val filter = if (cmdQuery.refTypes.hasNone()) null else ReferenceFilter { pool ->
            val result = cmdQuery.references.stream().allMatch { pool.contains(it) }
            result and cmdQuery.signatures.stream().allMatch { pool.toString().contains(it) }
        }
        dexFilter = DexFilter.MATCH_ALL
        classFilter = ClassFilter
            .builder()
            .setPackages(*cmdQuery.packages.toTypedArray())
            .setClasses(*cmdQuery.classes.toTypedArray())
            .setClassSimpleNames(*cmdQuery.classNames.toTypedArray())
            .setClassPattern(cmdQuery.classPattern)
            .setReferenceTypes(cmdQuery.refTypes)
            .setReferenceFilter(filter)
            .setSourceNames(*cmdQuery.sources.toTypedArray())
            .setNumbers(*cmdQuery.numbers.toTypedArray())
            .setModifiers(classAdvanced.modifiers)
            .setSuperClass(classAdvanced.superClass)
            .setInterfaces(classAdvanced.interfaces)
            .containsAnnotations(*classAdvanced.annotations.toTypedArray())
            .build()
        methodFilter = if (classMode) MethodFilter.MATCH_ALL else MethodFilter
            .builder()
            .setReferenceTypes(cmdQuery.refTypes)
            .setReferenceFilter(filter)
            .setNumbers(*cmdQuery.numbers.toTypedArray())
            .setModifiers(methodAdvanced.modifiers)
            .setMethodNames(*methodAdvanced.methodNames.toTypedArray())
            .setParamList(methodAdvanced.methodParams)
            .setReturnType(methodAdvanced.methodReturn)
            .setParamSize(methodAdvanced.methodParamSize)
            .containsAnnotations(*methodAdvanced.annotations.toTypedArray())
            .build()
        initialized = true
    }

    fun search(file: String): Set<String> {
        checkEngineState(true)
        try {
            return dexSearch(file)
        } catch (e: DexException) {
            CommandUtils.error("Failed", e)
        }
        return setOf()
    }

    private fun dexSearch(file: String): Set<String> {
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
            val (rClasses, numbers) = getNumbersWithResIds(this)
            val classFilter = classFilter.exclude(rClasses).resetNumbers(numbers)
            if (classMode) {
                onClassResult(dexFilter, classFilter) { handler.apply(it) }
            } else {
                val methodFilter = methodFilter.resetNumbers(numbers)
                onMethodResult(dexFilter, classFilter, methodFilter) { handler.apply(it) }
            }
        }
        if (results.isEmpty()) CommandUtils.print("Result:  [Not Found]")
        return results
    }

    private fun getNumbersWithResIds(dexplore: Dexplore): Pair<Set<String>, Array<Number>> {
        if (resourceNames.isEmpty()) return Pair(setOf(), arrayOf())
        val numbers = ArrayList<Number>()
        val resClasses = HashMap<String, ClassData>()
        resourceNames.forEach { res ->
            val resClass = resClasses.computeIfAbsent(res.substringBeforeLast('.')) {
                DexHelper.getClass(dexplore, it) ?: throw Exception("Class not found: $it")
            }
            val resName = res.substringAfterLast('.')
            numbers.add(
                DexHelper.getResId(resClass, resName) ?:
                throw Exception("Resource id couldn't retrieve: ${resClass.clazz}.$resName")
            )
        }
        numbers.addAll(numberLiterals)
        return Pair(resClasses.keys, numbers.toTypedArray())
    }

    private fun ClassFilter.resetNumbers(numbers: Array<Number>): ClassFilter {
        return if (numbers.isEmpty()) this else toBuilder().setNumbers(*numbers).build()
    }

    private fun MethodFilter.resetNumbers(numbers: Array<Number>): MethodFilter {
        return if (numbers.isEmpty()) this else toBuilder().setNumbers(*numbers).build()
    }

    private fun ClassFilter.exclude(classes: Set<String>): ClassFilter {
        return if (patternExists || classes.isEmpty()) this else toBuilder().setClassPattern(
            Pattern.compile("^(?!" + classes.joinToString("|") { "\\Q$it\\E" } + ").*\$")
        ).build()
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

    private fun checkEngineState(state: Boolean) {
        check(state == initialized) {
            if (state) "Engine is not initialized"
            else "Engine is already initialized"
        }
    }
}
