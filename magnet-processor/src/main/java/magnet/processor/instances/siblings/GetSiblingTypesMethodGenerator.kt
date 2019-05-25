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

package magnet.processor.instances.siblings

import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import magnet.processor.instances.GetSiblingTypesMethod
import magnet.processor.instances.AspectGenerator
import javax.lang.model.element.Modifier

internal class GetSiblingTypesMethodGenerator : AspectGenerator {

    private var getSiblingTypes: MethodSpec? = null
    private var constBuilder: FieldSpec? = null
    private var constInitializer: CodeBlock.Builder? = null
    private var typesLeft: Int = 0

    fun enterSiblingTypesMethod(method: GetSiblingTypesMethod) {
        getSiblingTypes = MethodSpec
            .methodBuilder("getSiblingTypes")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override::class.java)
            .returns(ArrayTypeName.of(Class::class.java))
            .addStatement("return SIBLING_TYPES")
            .build()
        constInitializer = CodeBlock.builder().add("{ ")
        typesLeft = method.siblingTypes.size
    }

    fun visitSiblingType(type: ClassName) {
        if (--typesLeft > 0) {
            checkNotNull(constInitializer).add("\$T.class, ", type)
        } else {
            checkNotNull(constInitializer).add("\$T.class }", type)
        }
    }

    fun exitSiblingTypesMethod() {
        constBuilder = FieldSpec
            .builder(ArrayTypeName.of(Class::class.java), "SIBLING_TYPES")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .initializer(checkNotNull(constInitializer).build())
            .build()
    }

    override fun reset() {
        getSiblingTypes = null
        constBuilder = null
        constInitializer = null
        typesLeft = 0
    }

    override fun generate(classBuilder: TypeSpec.Builder) {
        constBuilder?.let { classBuilder.addField(it) }
        getSiblingTypes?.let { classBuilder.addMethod(it) }
    }
}