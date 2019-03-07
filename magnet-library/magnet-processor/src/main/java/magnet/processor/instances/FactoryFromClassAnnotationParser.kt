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
import magnet.Instance
import magnet.processor.MagnetProcessorEnv
import magnet.processor.common.ValidationException
import magnet.processor.instances.kotlin.KotlinConstructorMetadata
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
                getSelectorMethod = getSelectorMethod,
                getSiblingTypesMethod = getSiblingTypesMethod
            )
        }
    }

    private fun parseCreateMethod(element: TypeElement): CreateMethod {

        val constructors = ElementFilter
            .constructorsIn(element.enclosedElements)
            .filterNot { it.modifiers.contains(Modifier.PRIVATE) || it.modifiers.contains(Modifier.PROTECTED) }

        if (constructors.size != 1) {
            throw ValidationException(
                element = element,
                message = "Classes annotated with ${Instance::class.java} must have exactly one " +
                    "public or package-protected constructor."
            )
        }

        val methodMetadata = KotlinConstructorMetadata(element)
        val methodParameters = mutableListOf<MethodParameter>()
        constructors[0].parameters.forEach { variable ->
            val methodParameter = parseMethodParameter(element, variable, methodMetadata)
            methodParameters.add(methodParameter)
        }

        return CreateMethod(
            methodParameters
        )
    }

}

private fun generateFactoryName(
    hasSiblingsTypes: Boolean, instanceType: ClassName, interfaceType: ClassName
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
