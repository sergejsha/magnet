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
import kotlinx.metadata.Flag
import kotlinx.metadata.Flags
import magnet.Instance
import magnet.processor.MagnetProcessorEnv
import magnet.processor.common.compilationError
import magnet.processor.common.validationError
import magnet.processor.instances.kotlin.CONSTRUCTOR_NAME
import magnet.processor.instances.kotlin.DefaultMetadata
import magnet.processor.instances.kotlin.FunctionSelector
import magnet.processor.instances.kotlin.MethodMeta
import magnet.processor.instances.kotlin.ParameterMeta
import magnet.processor.instances.kotlin.hasParameters
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter

internal class FactoryFromClassAnnotationParser(
    env: MagnetProcessorEnv
) : AnnotationParser<TypeElement>(env, true) {

    override fun parse(element: TypeElement): List<FactoryType> {

        val annotation = parseAnnotation(element)
        val instanceType = ClassName.get(element)
        val instancePackage = instanceType.packageName()

        return annotation.types.map {

            val hasSiblingTypes = annotation.types.size > 1
            val getSiblingTypesMethod = if (hasSiblingTypes) {
                val types = annotation.types - it
                val siblingTypes = mutableListOf<ClassName>()
                for (type in types) {
                    siblingTypes.add(type)
                    val factoryName = generateFactoryName(true, instanceType, type)
                    siblingTypes.add(ClassName.bestGuess("$instancePackage.$factoryName"))
                }
                GetSiblingTypesMethod(siblingTypes)
            } else {
                null
            }

            val selectorAttributes = selectorAttributeParser.convert(annotation.selector, element)
            val getSelectorMethod = if (selectorAttributes == null) null else GetSelectorMethod(selectorAttributes)
            val getLimitMethod = if (annotation.limitedTo.isEmpty()) null else GetLimitMethod(annotation.limitedTo)

            val factoryName = generateFactoryName(hasSiblingTypes, instanceType, it)
            FactoryType(
                element = element,
                interfaceType = it,
                classifier = annotation.classifier,
                scoping = annotation.scoping,
                disposerMethodName = annotation.disposer,
                disabled = annotation.disabled,
                customFactoryType = annotation.factory,
                implementationType = instanceType,
                factoryType = ClassName.bestGuess("$instancePackage.$factoryName"),
                createStatement = TypeCreateStatement(instanceType),
                createMethod = parseCreateMethod(element),
                getScopingMethod = GetScopingMethod(annotation.scoping),
                getLimitMethod = getLimitMethod,
                getSelectorMethod = getSelectorMethod,
                getSiblingTypesMethod = getSiblingTypesMethod
            )
        }
    }

    private fun parseCreateMethod(element: TypeElement): CreateMethod {

        val constructors = ElementFilter
            .constructorsIn(element.enclosedElements)
            .filterNot {
                it.modifiers.contains(Modifier.PRIVATE) ||
                    it.modifiers.contains(Modifier.PROTECTED)
            }

        val methodMeta: MethodMeta? = element
            .getAnnotation(Metadata::class.java)
            ?.let {
                DefaultMetadata(
                    metadata = it,
                    element = element,
                    functionSelector = ConstructorFunctionSelector(element, constructors)
                )
            }

        val methodParameters = mutableListOf<MethodParameter>().apply {
            val constructor =
                if (methodMeta == null) selectJavaConstructor(constructors, element)
                else selectKotlinConstructor(methodMeta)

            for (parameter in constructor.parameters) {
                add(parseMethodParameter(element, parameter, methodMeta))
            }
        }

        return CreateMethod(methodParameters)
    }
}

private fun Element.throwExactlyOneConstructorRequired(): Nothing =
    validationError(
        "Classes annotated with ${magnet.Instance::class.java} must have exactly one" +
            " public or package-private constructor."
    )

private fun generateFactoryName(
    hasSiblingsTypes: Boolean,
    instanceType: ClassName,
    interfaceType: ClassName
): String =
    if (hasSiblingsTypes) "${instanceType.getFullName()}${interfaceType.getFullName()}$FACTORY_SUFFIX"
    else "${instanceType.getFullName()}$FACTORY_SUFFIX"

private fun ClassName.getFullName(): String {
    if (enclosingClassName() == null) {
        return simpleName()
    }
    val nameBuilder = StringBuilder(simpleName())
    var typeClassName = this
    while (typeClassName.enclosingClassName() != null) {
        nameBuilder.insert(0, typeClassName.enclosingClassName().simpleName())
        typeClassName = typeClassName.enclosingClassName()
    }
    return nameBuilder.toString()
}

private fun selectJavaConstructor(constructors: List<ExecutableElement>, element: TypeElement): ExecutableElement =
    if (constructors.size == 1) constructors[0] else element.throwExactlyOneConstructorRequired()

private fun selectKotlinConstructor(methodMeta: MethodMeta): ExecutableElement =
    methodMeta.method

private class ConstructorFunctionSelector(
    private val element: TypeElement,
    private val constructors: List<ExecutableElement>
) : FunctionSelector {

    override val function: ExecutableElement
        get() = overloadConstructor
            ?.let { return it }
            ?: element.throwExactlyOneConstructorRequired()

    private var overloadConstructor: ExecutableElement? = null

    override fun visitFunction(flags: Flags, name: String): Boolean =
        Flag.Constructor.IS_PRIMARY(flags) && name == CONSTRUCTOR_NAME

    override fun acceptFunctionParameters(parameters: Map<String, ParameterMeta>): Map<String, ParameterMeta> {
        val overloadedParameters = parameters.filter { it.value.types.firstOrNull()?.default != true }
        overloadConstructor = constructors.find { it.hasParameters(overloadedParameters) }
        if (overloadConstructor == null) {
            val primaryConstructor = constructors.find { it.hasParameters(parameters) }
                ?: element.compilationError(
                    "Overloaded secondary constructor expected.\n" +
                        " Primary constructor: $parameters\n" +
                        " Secondary constructor: $overloadedParameters"
                )

            primaryConstructor.validationError(
                "Constructor with default arguments in a class annotated with ${Instance::class}" +
                    " must have @JmvOverloads annotation." +
                    " Use: class ${element.simpleName} @JvmOverloads constructor(...)"
            )
        }
        return overloadedParameters
    }
}
