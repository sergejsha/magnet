/*
 * Copyright (C) 2018-2021 Sergej Shafarenka, www.halfbit.de
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
import kotlinx.metadata.Flag
import kotlinx.metadata.Flags
import magnet.Instance
import magnet.processor.MagnetProcessorEnv
import magnet.processor.common.CONSTRUCTOR_NAME
import magnet.processor.common.DefaultKotlinMethodMetadata
import magnet.processor.common.FunctionSelector
import magnet.processor.common.KotlinMethodMetadata
import magnet.processor.common.ParameterMeta
import magnet.processor.common.hasParameters
import magnet.processor.common.throwCompilationError
import magnet.processor.common.throwValidationError
import magnet.processor.instances.CreateMethod
import magnet.processor.instances.FactoryType
import magnet.processor.instances.GetLimitMethod
import magnet.processor.instances.GetScopingMethod
import magnet.processor.instances.GetSelectorMethod
import magnet.processor.instances.GetSiblingTypesMethod
import magnet.processor.instances.MethodParameter
import magnet.processor.instances.TypeCreateStatement
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter

internal class InstanceParserForClass(
    env: MagnetProcessorEnv
) : InstanceParser<TypeElement>(env, true) {

    override fun generateFactories(instance: ParserInstance<TypeElement>): List<FactoryType> {

        val instanceType = ClassName.get(instance.element)
        val instancePackage = instanceType.packageName()

        return instance.types.map {

            val hasSiblingTypes = instance.types.size > 1
            val getSiblingTypesMethod = if (hasSiblingTypes) {
                val types = instance.types - it
                val siblingTypes = mutableListOf<ClassName>()
                for (type in types) {
                    siblingTypes.add(type)
                    val factoryName = generateFactoryName(true, instanceType, type)
                    siblingTypes.add(ClassName.bestGuess("$instancePackage.$factoryName"))
                }
                GetSiblingTypesMethod(siblingTypes)
            } else null

            val selectorAttributes = instance.selector
            val getSelectorMethod = if (selectorAttributes == null) null else GetSelectorMethod(selectorAttributes)
            val getLimitMethod = if (instance.limitedTo.isEmpty()) null else GetLimitMethod(instance.limitedTo)

            val factoryName = generateFactoryName(hasSiblingTypes, instanceType, it)
            FactoryType(
                element = instance.element,
                interfaceType = it,
                classifier = instance.classifier,
                scoping = instance.scoping,
                disposerMethodName = instance.disposer,
                disabled = instance.disabled,
                customFactoryType = instance.factory,
                implementationType = instanceType,
                factoryType = ClassName.bestGuess("$instancePackage.$factoryName"),
                createStatement = TypeCreateStatement(instanceType),
                createMethod = parseCreateMethod(instance.element),
                getScopingMethod = GetScopingMethod(instance.scoping),
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

        val methodMeta: KotlinMethodMetadata? = element
            .getAnnotation(Metadata::class.java)
            ?.let {
                DefaultKotlinMethodMetadata(
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
    throwValidationError(
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

private fun selectKotlinConstructor(methodMeta: KotlinMethodMetadata): ExecutableElement =
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
        name == CONSTRUCTOR_NAME && !Flag.Constructor.IS_SECONDARY(flags)

    override fun acceptFunctionParameters(parameters: Map<String, ParameterMeta>): Map<String, ParameterMeta> {
        val overloadedParameters = parameters.filter { it.value.types.firstOrNull()?.default != true }
        overloadConstructor = constructors.find { it.hasParameters(overloadedParameters) }
        if (overloadConstructor == null) {
            val primaryConstructor = constructors.find { it.hasParameters(parameters) }
                ?: element.throwCompilationError(
                    "Overloaded secondary constructor expected.\n" +
                        " Primary constructor: $parameters\n" +
                        " Secondary constructor: $overloadedParameters"
                )

            primaryConstructor.throwValidationError(
                "Constructor with default arguments in a class annotated with ${Instance::class}" +
                    " must have @JmvOverloads annotation." +
                    " Use: class ${element.simpleName} @JvmOverloads constructor(...)"
            )
        }
        return overloadedParameters
    }
}
