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
import magnet.processor.CompilationException
import magnet.processor.MagnetProcessorEnv
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter

internal class FactoryFromClassAnnotationParser(
    env: MagnetProcessorEnv
) : AnnotationParser(env) {

    fun parse(element: TypeElement): FactoryType {

        val instanceType = ClassName.get(element)
        val instancePackage = instanceType.packageName()
        val instanceName = instanceType.simpleName()
        val factoryType = ClassName.bestGuess("${instancePackage}.Magnet${instanceName}Factory")

        val annotation = parseAnnotation(element)
        val createMethod = parseConstructor(element)
        val retentionMethod = GetRetentionMethod(annotation.retention)
        val createStatement = TypeCreateStatement(instanceType)

        return FactoryType(
            element,
            factoryType,
            annotation.classifier,
            annotation.type,
            createStatement,
            createMethod,
            retentionMethod
        )
    }

    private fun parseConstructor(element: TypeElement): CreateMethod {

        val constructors = ElementFilter.constructorsIn(element.enclosedElements)
        if (constructors.size != 1) {
            env.reportError(element, "Exactly one constructor is required for $element")
            throw CompilationException()
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
