/*
 * Copyright (C) 2018-2019 Sergej Shafarenka, www.halfbit.de
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
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import magnet.Classifier
import magnet.Instance
import magnet.processor.MagnetProcessorEnv
import magnet.processor.common.CompilationException
import magnet.processor.common.ValidationException
import magnet.processor.common.validationError
import magnet.processor.instances.kotlin.DefaultMetadata
import magnet.processor.instances.kotlin.MethodFunctionSelector
import magnet.processor.instances.kotlin.MethodMeta
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/** Awesome static factory method parser. */
internal class FactoryFromMethodAnnotationParser(
    env: MagnetProcessorEnv
) : AnnotationParser<ExecutableElement>(env, false) {

    override fun parse(element: ExecutableElement): List<FactoryType> {

        if (!element.modifiers.contains(Modifier.STATIC)) {
            throw ValidationException(
                element = element,
                message = "Method annotated by ${Instance::class.java} must be 'static'"
            )
        }

        if (element.modifiers.contains(Modifier.PRIVATE)) {
            throw ValidationException(
                element = element,
                message = "Method annotated by ${Instance::class.java} must not be 'private'"
            )
        }

        val annotation = parseAnnotation(element)
        val staticMethodReturnType = TypeName.get(element.returnType)

        for (type in annotation.types) {
            if (type != staticMethodReturnType) {
                if (staticMethodReturnType is ParameterizedTypeName) {
                    if (annotation.classifier == Classifier.NONE) {
                        element.validationError(
                            "Method providing a parametrised type must have 'classifier' value" +
                                " set in @${Instance::class.java.simpleName} annotation."
                        )
                    }
                } else {
                    element.validationError(
                        "Method must return instance of ${type.reflectionName()} as declared" +
                            " by @${Instance::class.java.simpleName} annotation." +
                            " Returned type: $staticMethodReturnType."
                    )
                }
            }
        }

        val staticMethodClassName = ClassName.get(element.enclosingElement as TypeElement)
        val staticMethodName = element.simpleName.toString()
        val uniqueFactoryNameBuilder = StringBuilder()
            .append(staticMethodClassName.packageName())
            .append('.')
            .append(staticMethodClassName.simpleName().capitalize())
            .append(staticMethodName.capitalize())

        val topmostElement = element.getTopmostTypeElement()
        val methodMeta: MethodMeta? = topmostElement
            .getAnnotation(Metadata::class.java)
            ?.let {
                DefaultMetadata(
                    metadata = it,
                    element = topmostElement,
                    functionSelector = MethodFunctionSelector(element)
                )
            }

        val methodParameters = mutableListOf<MethodParameter>()
        element.parameters.forEach { variable ->
            val methodParameter = parseMethodParameter(element, variable, methodMeta)
            methodParameters.add(methodParameter)
            uniqueFactoryNameBuilder.append(methodParameter.name.capitalize())
        }

        val instanceFullName = uniqueFactoryNameBuilder.toString()
        return annotation.types.map {

            val isSingleTypeFactory = annotation.types.size == 1
            val getSiblingTypesMethod = if (isSingleTypeFactory) {
                null
            } else {
                val types = annotation.types - it
                val siblingTypes = mutableListOf<ClassName>()
                for (type in types) {
                    siblingTypes.add(type)
                    val factoryFullName = generateFactoryName(false, instanceFullName, type)
                    siblingTypes.add(ClassName.bestGuess(factoryFullName))
                }
                GetSiblingTypesMethod(siblingTypes)
            }

            val selectorAttributes = selectorAttributeParser.convert(annotation.selector, element)
            val getSelectorMethod = if (selectorAttributes == null) null else GetSelectorMethod(selectorAttributes)
            val getLimitMethod = if (annotation.limit.isEmpty()) null else GetLimitMethod(annotation.limit)

            val factoryFullName = generateFactoryName(isSingleTypeFactory, instanceFullName, it)
            FactoryType(
                element = element,
                interfaceType = it,
                classifier = annotation.classifier,
                scoping = annotation.scoping,
                disposerMethodName = annotation.disposer,
                disabled = annotation.disabled,
                customFactoryType = annotation.factory,
                implementationType = null,
                factoryType = ClassName.bestGuess(factoryFullName),
                createStatement = StaticMethodCreateStatement(staticMethodClassName, staticMethodName),
                createMethod = CreateMethod(methodParameters),
                getScopingMethod = GetScopingMethod(annotation.scoping),
                getLimitMethod = getLimitMethod,
                getSelectorMethod = getSelectorMethod,
                getSiblingTypesMethod = getSiblingTypesMethod
            )
        }
    }
}

private fun Element.getTopmostTypeElement(): TypeElement {
    var result: TypeElement? = null
    var element: Element? = this
    while (element != null) {
        if (element is TypeElement) {
            result = element
        }
        element = element.enclosingElement
    }
    return result
        ?: throw CompilationException(
            element = this,
            message = "Static method must be declared in a class."
        )
}

private fun generateFactoryName(isSingleTypeFactory: Boolean, instanceName: String, it: ClassName): String =
    if (isSingleTypeFactory) "${instanceName}MagnetFactory"
    else "$instanceName${it.simpleName()}MagnetFactory"
