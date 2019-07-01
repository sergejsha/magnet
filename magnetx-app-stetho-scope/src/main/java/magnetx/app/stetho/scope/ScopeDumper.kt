package magnetx.app.stetho.scope

import magnet.Classifier
import magnet.Scope
import magnet.inspection.Instance
import magnet.inspection.ScopeVisitor
import java.io.PrintStream

internal class ScopeDumper(private val writer: PrintStream) : ScopeVisitor {

    private val instances = mutableListOf<Instance>()
    private var currentScope: Scope? = null
    private var level = 0

    override fun onEnterScope(scope: Scope, parent: Scope?): Boolean {
        if (currentScope != null) {
            val indentScope = "   ".repeat(level - 1)
            writer.println("$indentScope [$level] $currentScope")

            instances.sortWith(compareBy({ it.provision }, { it.scoping }, { it.type.name }))
            val indentInstance = "   ".repeat(level)
            for (instance in instances) {
                val line = when (instance.provision) {
                    Instance.Provision.BOUND -> instance.renderBound()
                    Instance.Provision.INJECTED -> instance.renderInjected()
                }
                writer.println("$indentInstance $line")
            }
            instances.clear()
        }
        currentScope = scope
        level++
        return true
    }

    override fun onInstance(instance: Instance): Boolean {
        instances.add(instance)
        return true
    }

    override fun onExitScope(scope: Scope) {
        level--
    }
}

private fun Instance.renderBound(): String =
    if (classifier == Classifier.NONE) "$provision ${type.simpleName} $value"
    else "$provision ${type.name}@$classifier $value"

private fun Instance.renderInjected(): String =
    if (classifier == Classifier.NONE) "$scoping ${type.simpleName} $value"
    else "$scoping ${type.simpleName}@$classifier $value"
