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

package magnet.processor.factory

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element

const val PARAM_SCOPE_NAME = "scope"

interface FactoryTypeVisitor {
    fun visitEnter(factoryType: FactoryType)
    fun visitEnter(createMethod: CreateMethod)
    fun visit(parameter: MethodParameter)
    fun visitExit(createMethod: CreateMethod)
    fun visit(method: GetScopingMethod)
    fun visitExit(factory: FactoryType)
}

data class Annotation(
    val type: ClassName,
    val classifier: String,
    val scoping: String,
    val disabled: Boolean
)

data class FactoryType(
    val element: Element,
    val annotation: Annotation,
    val factoryType: ClassName,
    val createStatement: CreateStatement,
    val createMethod: CreateMethod,
    val getScopingMethod: GetScopingMethod
) {
    fun accept(visitor: FactoryTypeVisitor) {
        visitor.visitEnter(this)
        createMethod.accept(visitor)
        getScopingMethod.accept(visitor)
        visitor.visitExit(this)
    }
}

sealed class CreateStatement

data class TypeCreateStatement(
    val instanceType: ClassName
) : CreateStatement()

data class MethodCreateStatement(
    val staticMethodClassName: ClassName,
    val staticMethodName: String
) : CreateStatement()

data class CreateMethod(
    val methodParameter: List<MethodParameter>
) {
    fun accept(visitor: FactoryTypeVisitor) {
        visitor.visitEnter(this)
        methodParameter.forEach { parameterNode ->
            parameterNode.accept(visitor)
        }
        visitor.visitExit(this)
    }
}

data class MethodParameter(
    val name: String,
    val type: TypeName,
    val typeErased: Boolean,
    val classifier: String,
    val method: GetterMethod
) {
    fun accept(visitor: FactoryTypeVisitor) {
        visitor.visit(this)
    }
}

data class GetScopingMethod(
    val scoping: String
) {
    fun accept(visitor: FactoryTypeVisitor) {
        visitor.visit(this)
    }
}

enum class GetterMethod(val code: String) {

    GET_SINGLE("getSingle"),
    GET_OPTIONAL("getOptional"),
    GET_MANY("getMany"),
    GET_SCOPE("");

}
