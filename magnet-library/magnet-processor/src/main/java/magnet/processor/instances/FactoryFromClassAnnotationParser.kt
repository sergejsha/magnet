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
import kotlinx.metadata.Flag
import kotlinx.metadata.Flags
import kotlinx.metadata.KmClassVisitor
import kotlinx.metadata.KmConstructorVisitor
import kotlinx.metadata.KmTypeVisitor
import kotlinx.metadata.KmValueParameterVisitor
import kotlinx.metadata.KmVariance
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import magnet.Instance
import magnet.processor.MagnetProcessorEnv
import magnet.processor.common.ValidationException
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

        val kotlinHeader = element.getAnnotation(Metadata::class.java)?.let {
            KotlinClassHeader(
                it.kind,
                it.metadataVersion,
                it.bytecodeVersion,
                it.data1,
                it.data2,
                it.extraString,
                it.packageName,
                it.extraInt
            )
        }

        kotlinHeader?.let {
            val metadata = KotlinClassMetadata.read(it)
            if (metadata is KotlinClassMetadata.Class) {
                metadata.accept(object : KmClassVisitor() {
                    override fun visitConstructor(flags: Flags): KmConstructorVisitor? {
                        return if (flags.isPrimaryConstructor) {
                            object : KmConstructorVisitor() {
                                override fun visitValueParameter(flags: Flags, name: String): KmValueParameterVisitor? {
                                    return object : KmValueParameterVisitor() {
                                        override fun visitType(flags: Flags): KmTypeVisitor? {
                                            return TypeExtractorVisitor(flags)
                                        }
                                    }
                                }
                            }
                        } else null
                    }
                })
            }
        }

        // todo create a kotlin property fetcher class, for Lazy<(List)T(?)> properties

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

class TypeExtractorVisitor(
    private val flags: Flags,
    private val offset: Int = 0
) : KmTypeVisitor() {

    private val nullable = flags.isNullableType

    override fun visitClass(name: kotlinx.metadata.ClassName) {
        println(" ".repeat(offset) + "enter: $name, isNullable: $nullable")
    }

    override fun visitArgument(flags: Flags, variance: KmVariance): KmTypeVisitor? {
        return TypeExtractorVisitor(flags, offset + 2)
    }

    override fun visitTypeParameter(id: Int) {
        println(" ".repeat(offset) + "visitType: $id")
    }

    override fun visitEnd() {
        println(" ".repeat(offset) + "exit")
    }
}

internal val Flags.isPrimaryConstructor: Boolean get() = Flag.Constructor.IS_PRIMARY(this)
internal val Flags.isNullableType: Boolean get() = Flag.Type.IS_NULLABLE(this)

private fun generateFactoryName(
    hasSiblingsTypes: Boolean, instanceType: ClassName, interfaceType: ClassName
): String =
    if (hasSiblingsTypes) {
        "${instanceType.getFullName()}${interfaceType.getFullName()}$FACTORY_SUFFIX"
    } else {
        "${instanceType.getFullName()}$FACTORY_SUFFIX"
    }

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
