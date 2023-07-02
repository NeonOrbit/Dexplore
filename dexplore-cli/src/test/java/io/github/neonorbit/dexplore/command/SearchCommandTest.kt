/*
 * Copyright (C) 2023 NeonOrbit
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

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class SearchCommandTest : BaseJCommanderTest() {
    override fun newInstance(): Command {
        return SearchCommand()
    }

    @Test
    fun validateOptions() {
        Assertions.assertFalse(
            validate("search", "file")
        )
        Assertions.assertTrue(
            validate("search", "file", "-src", "dummy")
        )
        // TO-DO
    }

    @Test
    fun searchTest() {
        // run("search", "file", "-src", "dummy1")
        // run("search", "file", "-src", "dummy2")
        // TO-DO
    }
}
