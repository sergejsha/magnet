/*
 * Copyright (C) 2019 Sergej Shafarenka, www.halfbit.de
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

package magnet.processor.instances.kotlin

import kotlinx.metadata.ClassName
import kotlinx.metadata.Flag
import kotlinx.metadata.Flags
import kotlinx.metadata.KmClassVisitor
import kotlinx.metadata.KmConstructorVisitor
import kotlinx.metadata.KmFunctionVisitor
import kotlinx.metadata.KmPackageVisitor
import kotlinx.metadata.KmTypeVisitor
import kotlinx.metadata.KmValueParameterVisitor
import kotlinx.metadata.KmVariance
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import magnet.processor.common.CompilationException
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

interface MethodMetadata {
    fun getParamMeta(paramName: String, typeDepth: Int): TypeMeta
}

data class ParamMeta(
    val name: String,
    val types: List<TypeMeta>
)

data class TypeMeta(
    val type: String,
    val nullable: Boolean
)

interface FunctionSelector {
    fun visitFunction(flags: Flags, name: String): Boolean
    fun visitFunctionParameters(parameters: Map<String, ParamMeta>): Boolean
}

object PrimaryConstructorSelector : FunctionSelector {
    override fun visitFunction(flags: Flags, name: String) = flags.isPrimaryConstructor
    override fun visitFunctionParameters(parameters: Map<String, ParamMeta>) = true
}

class NamedFunctionSelector(private val element: ExecutableElement) : FunctionSelector {
    override fun visitFunction(flags: Flags, name: String) =
        element.simpleName.toString() == name

    override fun visitFunctionParameters(parameters: Map<String, ParamMeta>): Boolean {
        if (parameters.size != element.parameters.size) {
            return false
        }
        parameters.values.forEachIndexed { index, paramMeta ->
            if (element.parameters[index].simpleName.toString() != paramMeta.name) {
                return false
            }
        }
        return true
    }
}

internal class KotlinConstructorMetadata(
    private val metadataAnnotation: Metadata,
    private val element: TypeElement,
    private val functionSelector: FunctionSelector
) : MethodMetadata {

    private val paramMetas: Map<String, ParamMeta> by lazy {
        val metadata = with(metadataAnnotation) {
            KotlinClassMetadata.read(
                KotlinClassHeader(
                    kind,
                    metadataVersion,
                    bytecodeVersion,
                    data1,
                    data2,
                    extraString,
                    packageName,
                    extraInt
                )
            )
        }

        when (metadata) {
            is KotlinClassMetadata.Class ->
                AnnotatedClassVisitor(functionSelector).let {
                    metadata.accept(it)
                    it.parameters
                }
            is KotlinClassMetadata.FileFacade ->
                AnnotatedPackageVisitor(functionSelector).let {
                    metadata.accept(it)
                    it.parameters
                }
            else -> throw CompilationException(
                element = element,
                message = "Expecting 'KotlinClassMetadata.Class' while $metadata received."
            )
        }
    }

    override fun getParamMeta(paramName: String, typeDepth: Int): TypeMeta {

        val paramMeta = paramMetas[paramName]
            ?: throw CompilationException(
                element = element,
                message = "Cannot find parameter '$paramName' in metadata of $element."
            )

        if (typeDepth >= paramMeta.types.size) {
            throw CompilationException(
                element = element,
                message = "Cannot find TypeMeta depth of $typeDepth in ${paramMeta.types}."
            )
        }
        return paramMeta.types[typeDepth]
    }

}

private class AnnotatedPackageVisitor(
    private val functionSelector: FunctionSelector
) : KmPackageVisitor() {
    val parameters = mutableMapOf<String, ParamMeta>()

    override fun visitFunction(flags: Flags, name: String): KmFunctionVisitor? {
        return if (functionSelector.visitFunction(flags, name)) {
            parameters.clear()
            object : KmFunctionVisitor() {
                override fun visitValueParameter(flags: Flags, name: String): KmValueParameterVisitor? {
                    return object : KmValueParameterVisitor() {
                        override fun visitType(flags: Flags): KmTypeVisitor? {
                            return TypeExtractorVisitor(flags) { typeMeta ->
                                parameters[name] = ParamMeta(name, typeMeta)
                            }
                        }
                    }
                }

                override fun visitEnd() {
                    if (!functionSelector.visitFunctionParameters(parameters)) {
                        parameters.clear()
                    }
                }
            }
        } else null
    }
}

private class AnnotatedClassVisitor(
    private val functionSelector: FunctionSelector
) : KmClassVisitor() {
    val parameters = mutableMapOf<String, ParamMeta>()

    override fun visitConstructor(flags: Flags): KmConstructorVisitor? {
        return if (functionSelector.visitFunction(flags, "constructor")) {
            parameters.clear()
            object : KmConstructorVisitor() {
                override fun visitValueParameter(flags: Flags, name: String): KmValueParameterVisitor? {
                    return object : KmValueParameterVisitor() {
                        override fun visitType(flags: Flags): KmTypeVisitor? {
                            return TypeExtractorVisitor(flags) { typeMeta ->
                                parameters[name] = ParamMeta(name, typeMeta)
                            }
                        }
                    }
                }

                override fun visitEnd() {
                    if (!functionSelector.visitFunctionParameters(parameters)) {
                        parameters.clear()
                    }
                }
            }
        } else null
    }

    override fun visitFunction(flags: Flags, name: String): KmFunctionVisitor? {
        return if (functionSelector.visitFunction(flags, name)) {
            parameters.clear()
            object : KmFunctionVisitor() {
                override fun visitValueParameter(flags: Flags, name: String): KmValueParameterVisitor? {
                    return object : KmValueParameterVisitor() {
                        override fun visitType(flags: Flags): KmTypeVisitor? {
                            return TypeExtractorVisitor(flags) { typeMeta ->
                                parameters[name] = ParamMeta(name, typeMeta)
                            }
                        }
                    }
                }

                override fun visitEnd() {
                    if (!functionSelector.visitFunctionParameters(parameters)) {
                        parameters.clear()
                    }
                }
            }
        } else null
    }
}

class TypeExtractorVisitor(
    private val flags: Flags,
    private val typeMeta: MutableList<TypeMeta> = mutableListOf(),
    private val onVisitEnd: OnVisitEnd? = null
) : KmTypeVisitor() {

    override fun visitClass(name: ClassName) {
        typeMeta.add(TypeMeta(name, flags.isNullableType))
    }

    override fun visitArgument(flags: Flags, variance: KmVariance): KmTypeVisitor? =
        TypeExtractorVisitor(flags, typeMeta)

    override fun visitEnd() {
        onVisitEnd?.invoke(typeMeta)
    }
}

typealias OnVisitEnd = (List<TypeMeta>) -> Unit

internal val Flags.isPrimaryConstructor: Boolean get() = Flag.Constructor.IS_PRIMARY(this)
internal val Flags.isNullableType: Boolean get() = Flag.Type.IS_NULLABLE(this)
