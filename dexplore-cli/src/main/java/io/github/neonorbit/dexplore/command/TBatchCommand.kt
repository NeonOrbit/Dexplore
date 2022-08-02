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

@Parameters(commandNames = [TBatchCommand.NAME], commandDescription = TBatchCommand.DESC)
internal class TBatchCommand : Command {
    companion object {
        const val NAME = "batch"
        const val DESC = "Perform multiple search at once"
    }

    @Parameter(description = "files")
    var files: List<String> = ArrayList()

    @Parameter(
        order = 1,
        names = ["-f", "--file"],
        description = "Read queries from file: [query per line]"
    )
    var file = ""

    @Parameter(
        order = 2,
        variableArity = true,
        names = ["-q", "--queries"],
        description = "Multiple queries: separated by semicolon [;]"
    )
    var queries = ArrayList<String>()

    override fun apply() {
        TODO("Not yet implemented")
    }

    override fun validate(): Boolean {
        apply()  // Warn TODO
        if (files.isEmpty()) {
            CommandUtils.error("\n  Please provide input files\n")
            return false
        }
        if (file.isEmpty() && queries.isEmpty()) {
            CommandUtils.error("\n  Please provide search queries\n")
            return false
        }
        return true
    }
}
