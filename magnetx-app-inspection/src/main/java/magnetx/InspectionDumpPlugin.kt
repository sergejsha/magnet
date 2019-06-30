package magnetx

import com.facebook.stetho.dumpapp.ArgsHelper
import com.facebook.stetho.dumpapp.DumpUsageException
import com.facebook.stetho.dumpapp.DumperContext
import com.facebook.stetho.dumpapp.DumperPlugin
import magnet.Scope
import java.io.PrintStream

internal class InspectionDumpPlugin(
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
        scope.accept(ScopeWriter(writer), depth)
    }

    private fun doUsage(writer: PrintStream) {
        with(writer) {
            println("Usage: dumpapp scope <depth?>")
            println()
        }
    }
}

private const val NAME_MAGNET = "magnet"
private const val CMD_SCOPE = "scope"