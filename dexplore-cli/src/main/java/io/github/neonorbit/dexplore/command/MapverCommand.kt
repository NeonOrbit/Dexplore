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

@Parameters(commandNames = [MapverCommand.NAME], commandDescription = MapverCommand.DESC)
internal class MapverCommand : Command {
    companion object {
        const val NAME = "mapver"
        const val DESC = "Map classes from one version to another"
    }

    @Parameter(
            order = 1,
            names = ["-s", "--source"],
            description = "Source version (file) to map from"
    )
    var source = ""

    @Parameter(
            order = 2,
            variableArity = true,
            names = ["-c", "--classes"],
            description = "List of classes to map"
    )
    var classes = ArrayList<String>()

    @Parameter(
            order = 3,
            variableArity = true,
            names = ["-t", "--target"],
            description = "Target version (files) to map into"
    )
    var target = ArrayList<String>()

    override fun apply() {
        TODO("Not yet implemented")
    }

    override fun validate(): Boolean {
        apply()  // Warn TODO
        if (source.isEmpty()) {
            CommandUtils.error("\n  Please provide a source file\n")
            return false
        }
        if (target.isEmpty()) {
            CommandUtils.error("\n  Please provide target files\n")
            return false
        }
        if (classes.isEmpty()) {
            CommandUtils.error("\n  Please provide classes to map\n")
            return false
        }
        return true
    }
}
