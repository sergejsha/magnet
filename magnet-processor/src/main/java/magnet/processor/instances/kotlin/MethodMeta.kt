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
import magnet.processor.common.compilationError
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

interface MethodMeta {
    val method: ExecutableElement
    fun getTypeMeta(parameterName: String, typeDepth: Int): TypeMeta
}

data class ParameterMeta(
    val name: String,
    val types: List<TypeMeta>
)

data class TypeMeta(
    val type: String,
    val nullable: Boolean,
    val default: Boolean
)

interface FunctionSelector {
    val function: ExecutableElement
    fun visitFunction(flags: Flags, name: String): Boolean
    fun acceptFunctionParameters(parameters: Map<String, ParameterMeta>): Map<String, ParameterMeta>
}

internal class MethodFunctionSelector(
    override val function: ExecutableElement
) : FunctionSelector {

    override fun visitFunction(flags: Flags, name: String) =
        function.simpleName.toString() == name

    override fun acceptFunctionParameters(parameters: Map<String, ParameterMeta>): Map<String, ParameterMeta> {
        if (parameters.size != function.parameters.size) {
            return emptyMap()
        }
        return if (function.hasParameters(parameters)) parameters else emptyMap()
    }
}

fun ExecutableElement.hasParameters(parameters: Map<String, ParameterMeta>): Boolean {
    if (parameters.values.size != this.parameters.size) return false
    parameters.values.forEachIndexed { index, parameterMeta ->
        if (this.parameters[index].simpleName.toString() != parameterMeta.name) {
            return false
        }
    }
    return true
}

internal class DefaultMetadata(
    metadata: Metadata,
    private val element: TypeElement,
    private val functionSelector: FunctionSelector
) : MethodMeta {

    override lateinit var method: ExecutableElement

    private val parameterMetas: Map<String, ParameterMeta> =
        with(metadata) {
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
        }.let { kotlinMetadata ->
            when (kotlinMetadata) {
                is KotlinClassMetadata.Class ->
                    AnnotatedClassVisitor(functionSelector).let {
                        kotlinMetadata.accept(it)
                        method = functionSelector.function
                        it.parameters
                    }
                is KotlinClassMetadata.FileFacade ->
                    AnnotatedPackageVisitor(functionSelector).let {
                        kotlinMetadata.accept(it)
                        method = functionSelector.function
                        it.parameters
                    }
                else -> throw CompilationException(
                    element = element,
                    message = "Unsupported KotlinClassMetadata of type $kotlinMetadata"
                )
            }
        }

    override fun getTypeMeta(parameterName: String, typeDepth: Int): TypeMeta {
        val parameterMeta = parameterMetas[parameterName]
            ?: element.compilationError(
                "Cannot find parameter '$parameterName' in metadata of $element." +
                    " Available parameters: $parameterMetas"
            )
        if (typeDepth >= parameterMeta.types.size) {
            element.compilationError(
                "Cannot find TypeMeta depth of $typeDepth in ${parameterMeta.types}."
            )
        }
        return parameterMeta.types[typeDepth]
    }
}

private class AnnotatedPackageVisitor(
    private val functionSelector: FunctionSelector
) : KmPackageVisitor() {
    var parameters = emptyMap<String, ParameterMeta>()

    override fun visitFunction(flags: Flags, name: String): KmFunctionVisitor? {
        return if (functionSelector.visitFunction(flags, name)) {
            val visitedParameters = mutableMapOf<String, ParameterMeta>()

            object : KmFunctionVisitor() {
                override fun visitValueParameter(flags: Flags, name: String): KmValueParameterVisitor? {
                    val valueFlags = flags
                    return object : KmValueParameterVisitor() {
                        override fun visitType(flags: Flags): KmTypeVisitor? {
                            return TypeExtractorVisitor(valueFlags, flags) { typeMeta ->
                                visitedParameters[name] = ParameterMeta(name, typeMeta)
                            }
                        }
                    }
                }

                override fun visitEnd() {
                    parameters = functionSelector.acceptFunctionParameters(visitedParameters)
                }
            }
        } else null
    }
}

private class AnnotatedClassVisitor(
    private val functionSelector: FunctionSelector
) : KmClassVisitor() {
    var parameters = emptyMap<String, ParameterMeta>()

    override fun visitConstructor(flags: Flags): KmConstructorVisitor? {
        return if (functionSelector.visitFunction(flags, CONSTRUCTOR_NAME)) {
            val visitedParameters = mutableMapOf<String, ParameterMeta>()

            object : KmConstructorVisitor() {
                override fun visitValueParameter(flags: Flags, name: String): KmValueParameterVisitor? {
                    val valueFlags = flags
                    return object : KmValueParameterVisitor() {
                        override fun visitType(flags: Flags): KmTypeVisitor? {
                            return TypeExtractorVisitor(valueFlags, flags) { typeMeta ->
                                visitedParameters[name] = ParameterMeta(name, typeMeta)
                            }
                        }
                    }
                }

                override fun visitEnd() {
                    parameters = functionSelector.acceptFunctionParameters(visitedParameters)
                }
            }
        } else null
    }

    override fun visitFunction(flags: Flags, name: String): KmFunctionVisitor? {
        return if (functionSelector.visitFunction(flags, name)) {
            val visitedParameters = mutableMapOf<String, ParameterMeta>()

            object : KmFunctionVisitor() {
                override fun visitValueParameter(flags: Flags, name: String): KmValueParameterVisitor? {
                    val valueFlags = flags
                    return object : KmValueParameterVisitor() {
                        override fun visitType(flags: Flags): KmTypeVisitor? {
                            return TypeExtractorVisitor(valueFlags, flags) { typeMeta ->
                                visitedParameters[name] = ParameterMeta(name, typeMeta)
                            }
                        }
                    }
                }

                override fun visitEnd() {
                    parameters = functionSelector.acceptFunctionParameters(visitedParameters)
                }
            }
        } else null
    }
}

private class TypeExtractorVisitor(
    private val valueFlags: Flags,
    private val typeFlags: Flags,
    private val typeMeta: MutableList<TypeMeta> = mutableListOf(),
    private val onVisitEnd: OnVisitEnd? = null
) : KmTypeVisitor() {

    override fun visitClass(name: ClassName) {
        Flag.ValueParameter.DECLARES_DEFAULT_VALUE(valueFlags).let { default ->
            typeMeta.add(
                TypeMeta(
                    type = name,
                    nullable = Flag.Type.IS_NULLABLE(typeFlags),
                    default = default
                )
            )
        }
    }

    override fun visitArgument(flags: Flags, variance: KmVariance): KmTypeVisitor? =
        TypeExtractorVisitor(valueFlags, flags, typeMeta)

    override fun visitEnd() {
        onVisitEnd?.invoke(typeMeta)
    }
}

const val CONSTRUCTOR_NAME = "<init>"
private typealias OnVisitEnd = (List<TypeMeta>) -> Unit
