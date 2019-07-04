/*
 * Copyright (C) 2018 Sergej Shafarenka, www.halfbit.de
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

package magnet.processor.instances

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element

const val PARAM_SCOPE_NAME = "scope"

interface FactoryTypeVisitor {

    fun enterFactoryClass(factoryType: FactoryType) {}

    fun enterCreateMethod(createMethod: CreateMethod) {}
    fun visitCreateMethodParameter(parameter: MethodParameter) {}
    fun exitCreateMethod(createMethod: CreateMethod) {}

    fun visit(method: GetScopingMethod) {}
    fun visit(method: GetLimitMethod) {}

    fun enterSiblingTypesMethod(method: GetSiblingTypesMethod) {}
    fun visitSiblingType(type: ClassName) {}
    fun exitSiblingTypesMethod(method: GetSiblingTypesMethod) {}

    fun enterGetSelectorMethod(method: GetSelectorMethod) {}
    fun visitSelectorArgument(argument: String) {}
    fun exitGetSelectorMethod(method: GetSelectorMethod) {}

    fun exitFactoryClass(factory: FactoryType) {}
}

class Annotation(
    val types: List<ClassName>,
    val classifier: String,
    val scoping: String,
    val limit: String,
    val selector: String,
    val factory: TypeName?,
    val disposer: String?,
    val disabled: Boolean
)

class FactoryType(
    val element: Element,
    val interfaceType: ClassName,
    val classifier: String,
    val scoping: String,
    val disabled: Boolean,
    val factoryType: ClassName,
    val implementationType: ClassName?,
    val customFactoryType: TypeName?,
    val disposerMethodName: String?,
    val createStatement: CreateStatement,
    val createMethod: CreateMethod,
    val getScopingMethod: GetScopingMethod,
    val getLimitMethod: GetLimitMethod?,
    val getSelectorMethod: GetSelectorMethod?,
    val getSiblingTypesMethod: GetSiblingTypesMethod?
) {
    fun accept(visitor: FactoryTypeVisitor) {
        visitor.enterFactoryClass(this)
        createMethod.accept(visitor)
        getScopingMethod.accept(visitor)
        getLimitMethod?.accept(visitor)
        getSiblingTypesMethod?.accept(visitor)
        getSelectorMethod?.accept(visitor)
        visitor.exitFactoryClass(this)
    }
}

abstract class CreateStatement

class TypeCreateStatement(
    val instanceType: ClassName
) : CreateStatement()

class StaticMethodCreateStatement(
    val staticMethodClassName: ClassName,
    val staticMethodName: String
) : CreateStatement()

class CreateMethod(
    val methodParameter: List<MethodParameter>
) {
    fun accept(visitor: FactoryTypeVisitor) {
        visitor.enterCreateMethod(this)
        methodParameter.forEach { parameterNode ->
            parameterNode.accept(visitor)
        }
        visitor.exitCreateMethod(this)
    }
}

enum class Cardinality {
    Single, Optional, Many
}

sealed class Expression {
    object Scope : Expression()
    data class Getter(val cardinality: Cardinality) : Expression()
    data class LazyGetter(val cardinality: Cardinality) : Expression()
}

data class MethodParameter(
    val name: String,
    val expression: Expression,
    val returnType: TypeName,
    val parameterType: TypeName,
    val classifier: String,
    val typeErased: Boolean
) {
    fun accept(visitor: FactoryTypeVisitor) {
        visitor.visitCreateMethodParameter(this)
    }
}

class GetScopingMethod(val scoping: String) {
    fun accept(visitor: FactoryTypeVisitor) {
        visitor.visit(this)
    }
}

class GetLimitMethod(val limit: String) {
    fun accept(visitor: FactoryTypeVisitor) {
        visitor.visit(this)
    }
}

class GetSiblingTypesMethod(val siblingTypes: List<ClassName>) {
    fun accept(visitor: FactoryTypeVisitor) {
        visitor.enterSiblingTypesMethod(this)
        for (siblingType in siblingTypes) {
            visitor.visitSiblingType(siblingType)
        }
        visitor.exitSiblingTypesMethod(this)
    }
}

class GetSelectorMethod(val selectorArguments: List<String>) {
    fun accept(visitor: FactoryTypeVisitor) {
        visitor.enterGetSelectorMethod(this)
        for (argument in selectorArguments) {
            visitor.visitSelectorArgument(argument)
        }
        visitor.exitGetSelectorMethod(this)
    }
}
