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

import magnet.Classifier
import magnet.Scope
import magnet.Scope.Visitor.Instance
import java.io.PrintStream

internal class ScopeDumper(private val writer: PrintStream) : Scope.Visitor {

    private val instances = mutableListOf<Instance>()
    private var currentScope: Scope? = null
    private var level = 0

    override fun onEnterScope(scope: Scope, parent: Scope?): Boolean {
        currentScope?.let { instances.dump(it, level, writer) }
        currentScope = scope
        level++
        return true
    }

    override fun onInstance(instance: Instance): Boolean {
        instances.add(instance)
        return true
    }

    override fun onExitScope(scope: Scope) {
        currentScope?.let { instances.dump(it, level, writer) }
        currentScope = null
        level--
    }
}

private fun MutableList<Instance>.dump(scope: Scope, level: Int, writer: PrintStream) {
    val indentScope = "   ".repeat(level - 1)
    writer.println("$indentScope [$level] $scope")

    sortWith(compareBy({ it.provision }, { it.scoping }, { it.type.name }))
    val indentInstance = "   ".repeat(level)
    for (instance in this) {
        val line = when (instance.provision) {
            Instance.Provision.BOUND -> instance.renderBound()
            Instance.Provision.INJECTED -> instance.renderInjected()
        }
        writer.println("$indentInstance $line")
    }
    clear()
}

private fun Instance.renderBound(): String =
    if (classifier == Classifier.NONE) "$provision ${type.simpleName} $value"
    else "$provision ${type.name}@$classifier $value"

private fun Instance.renderInjected(): String =
    if (classifier == Classifier.NONE) "$scoping ${type.simpleName} $value"
    else "$scoping ${type.simpleName}@$classifier $value"
