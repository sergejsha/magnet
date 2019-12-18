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

package magnet.processor.instances.parser

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import magnet.Classifier
import magnet.Instance
import magnet.processor.MagnetProcessorEnv
import magnet.processor.common.CompilationException
import magnet.processor.common.DefaultKotlinMethodMetadata
import magnet.processor.common.KotlinMethodMetadata
import magnet.processor.common.MethodFunctionSelector
import magnet.processor.common.ValidationException
import magnet.processor.common.throwValidationError
import magnet.processor.instances.CreateMethod
import magnet.processor.instances.FactoryType
import magnet.processor.instances.GetLimitMethod
import magnet.processor.instances.GetScopingMethod
import magnet.processor.instances.GetSelectorMethod
import magnet.processor.instances.GetSiblingTypesMethod
import magnet.processor.instances.MethodParameter
import magnet.processor.instances.StaticMethodCreateStatement
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/** Awesome static factory method parser. */
internal class AnnotatedMethodInstanceParser(
    env: MagnetProcessorEnv
) : InstanceParser<ExecutableElement>(env, false) {

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

        val instance = parseInstance(element)
        val staticMethodReturnType = TypeName.get(element.returnType)

        for (type in instance.types) {
            if (type != staticMethodReturnType) {
                if (staticMethodReturnType is ParameterizedTypeName) {
                    if (instance.classifier == Classifier.NONE) {
                        element.throwValidationError(
                            "Method providing a parametrised type must have 'classifier' value" +
                                " set in @${Instance::class.java.simpleName} annotation."
                        )
                    }
                } else {
                    element.throwValidationError(
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
        val methodMeta: KotlinMethodMetadata? = topmostElement
            .getAnnotation(Metadata::class.java)
            ?.let {
                DefaultKotlinMethodMetadata(
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
        return instance.types.map {

            val isSingleTypeFactory = instance.types.size == 1
            val getSiblingTypesMethod = if (isSingleTypeFactory) {
                null
            } else {
                val types = instance.types - it
                val siblingTypes = mutableListOf<ClassName>()
                for (type in types) {
                    siblingTypes.add(type)
                    val factoryFullName = generateFactoryName(false, instanceFullName, type)
                    siblingTypes.add(ClassName.bestGuess(factoryFullName))
                }
                GetSiblingTypesMethod(siblingTypes)
            }

            val selectorAttributes = instance.selector
            val getSelectorMethod = if (selectorAttributes == null) null else GetSelectorMethod(selectorAttributes)
            val getLimitMethod = if (instance.limitedTo.isEmpty()) null else GetLimitMethod(instance.limitedTo)

            val factoryFullName = generateFactoryName(isSingleTypeFactory, instanceFullName, it)
            FactoryType(
                element = element,
                interfaceType = it,
                classifier = instance.classifier,
                scoping = instance.scoping,
                disposerMethodName = instance.disposer,
                disabled = instance.disabled,
                customFactoryType = instance.factory,
                implementationType = null,
                factoryType = ClassName.bestGuess(factoryFullName),
                createStatement = StaticMethodCreateStatement(staticMethodClassName, staticMethodName),
                createMethod = CreateMethod(methodParameters),
                getScopingMethod = GetScopingMethod(instance.scoping),
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
