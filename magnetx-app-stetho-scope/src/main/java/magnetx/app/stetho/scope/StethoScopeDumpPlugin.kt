/*
 * Copyright (C) 2019 Sergej Shafarenka, www.halfbit.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package magnetx.app.stetho.scope

import com.facebook.stetho.dumpapp.ArgsHelper
import com.facebook.stetho.dumpapp.DumpUsageException
import com.facebook.stetho.dumpapp.DumperContext
import com.facebook.stetho.dumpapp.DumperPlugin
import magnet.Scope
import java.io.PrintStream

internal class StethoScopeDumpPlugin(
    private val scope: Scope
) : DumperPlugin {

    override fun getName(): String = NAME_MAGNET

    override fun dump(dumpContext: DumperContext) {
        val writer: PrintStream = dumpContext.stdout
        val args: Iterator<String> = dumpContext.argsAsList.iterator()

        val command: String? = ArgsHelper.nextOptionalArg(args, null)
        when (command) {
            CMD_SCOPE -> doDumpScopes(args, writer)
            else -> {
                doUsage(writer)
                if (command != null) {
                    throw DumpUsageException("Unknown command: $command")
                }
            }
        }
    }

    private fun doDumpScopes(args: Iterator<String>, writer: PrintStream) {
        val depth = ArgsHelper.nextOptionalArg(args, null)?.toInt() ?: Integer.MAX_VALUE
        scope.accept(ScopeDumper(writer), depth)
    }

    private fun doUsage(writer: PrintStream) {
        with(writer) {
            println("Usage: ./dumpapp magnet scope [<depth:Int>]")
            println()
        }
    }
}

private const val NAME_MAGNET = "magnet"
private const val CMD_SCOPE = "scope"