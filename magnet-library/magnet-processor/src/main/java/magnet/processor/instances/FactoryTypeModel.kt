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
    val selector: String,
    val factory: TypeName?,
    val disabled: Boolean
)

class FactoryType(
    val element: Element,
    val type: ClassName,
    val classifier: String,
    val scoping: String,
    val disabled: Boolean,
    val factoryType: ClassName,
    val customFactoryType: TypeName?,
    val createStatement: CreateStatement,
    val createMethod: CreateMethod,
    val getScopingMethod: GetScopingMethod,
    val getSelectorMethod: GetSelectorMethod?,
    val getSiblingTypesMethod: GetSiblingTypesMethod?
) {
    fun accept(visitor: FactoryTypeVisitor) {
        visitor.enterFactoryClass(this)
        createMethod.accept(visitor)
        getScopingMethod.accept(visitor)
        getSiblingTypesMethod?.accept(visitor)
        getSelectorMethod?.accept(visitor)
        visitor.exitFactoryClass(this)
    }
}

abstract class CreateStatement

class TypeCreateStatement(
    val instanceType: ClassName
) : CreateStatement()

class MethodCreateStatement(
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

class MethodParameter(
    val name: String,
    val type: TypeName,
    val typeErased: Boolean,
    val classifier: String,
    val method: GetterMethod
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

enum class GetterMethod(val code: String) {

    GET_SINGLE("getSingle"),
    GET_OPTIONAL("getOptional"),
    GET_MANY("getMany"),
    GET_SCOPE("");

}
