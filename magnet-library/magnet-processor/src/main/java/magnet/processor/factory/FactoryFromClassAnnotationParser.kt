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
import magnet.Instance
import magnet.processor.MagnetProcessorEnv
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter

internal class FactoryFromClassAnnotationParser(
    env: MagnetProcessorEnv
) : AnnotationParser<TypeElement>(env, true) {

    override fun parse(element: TypeElement): List<FactoryType> {

        val annotation = parseAnnotation(element)
        val instanceType = ClassName.get(element)
        val instancePackage = instanceType.packageName()
        val instanceName = instanceType.simpleName()

        return annotation.types.map {

            val getSiblingTypesMethod = if (annotation.types.size == 1) null
            else GetSiblingTypesMethod(annotation.types - it)

            val factoryName = generateFactoryName(annotation, instanceName, it)
            FactoryType(
                element = element,
                type = it,
                classifier = annotation.classifier,
                scoping = annotation.scoping,
                disabled = annotation.disabled,
                factoryType = ClassName.bestGuess("$instancePackage.$factoryName"),
                createStatement = TypeCreateStatement(instanceType),
                createMethod = parseCreateMethod(element),
                getScopingMethod = GetScopingMethod(annotation.scoping),
                getSiblingTypesMethod = getSiblingTypesMethod
            )
        }
    }

    private fun parseCreateMethod(element: TypeElement): CreateMethod {

        val constructors = ElementFilter.constructorsIn(element.enclosedElements)
        if (constructors.size != 1) {
            throw env.compilationError(element, "Classes annotated with ${Instance::class.java}"
                + " must have exactly one constructor.")
        }

        val methodParameters = mutableListOf<MethodParameter>()
        constructors[0].parameters.forEach { variable ->
            val methodParameter = parseMethodParameter(element, variable)
            methodParameters.add(methodParameter)
        }

        return CreateMethod(
            methodParameters
        )
    }

}
