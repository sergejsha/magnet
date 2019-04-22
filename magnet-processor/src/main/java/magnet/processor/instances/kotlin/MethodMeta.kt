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
    fun getParamMeta(paramName: String, typeDepth: Int): TypeMeta
}

data class ParamMeta(
    val name: String,
    val types: List<TypeMeta>
)

data class TypeMeta(
    val type: String,
    val nullable: Boolean,
    val default: Boolean
)

interface FunctionSelector {
    fun visitFunction(flags: Flags, name: String): Boolean
    fun acceptFunctionParameters(parameters: Map<String, ParamMeta>): Boolean
}

object ConstructorWithDefaultArgumentsSelector : FunctionSelector {
    private var count = Int.MAX_VALUE

    override fun visitFunction(flags: Flags, name: String): Boolean {
        count = Int.MAX_VALUE
        return name == CONSTRUCTOR_NAME
    }

    override fun acceptFunctionParameters(parameters: Map<String, ParamMeta>): Boolean {
        return if (count > parameters.size) {
            count = parameters.size
            true
        } else false
    }
}

class ExecutableFunctionSelector(
    private val element: ExecutableElement,
    private val functionName: String = element.simpleName.toString()
) : FunctionSelector {

    override fun visitFunction(flags: Flags, name: String) =
        functionName == name

    override fun acceptFunctionParameters(parameters: Map<String, ParamMeta>): Boolean {
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
) : MethodMeta {

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
            ?: element.compilationError(
                "Cannot find parameter '$paramName' in metadata of $element." +
                    " Available parameters: $paramMetas"
            )
        if (typeDepth >= paramMeta.types.size) {
            element.compilationError(
                "Cannot find TypeMeta depth of $typeDepth in ${paramMeta.types}."
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
                    val valueFlags = flags
                    return object : KmValueParameterVisitor() {
                        override fun visitType(flags: Flags): KmTypeVisitor? {
                            return TypeExtractorVisitor(valueFlags, flags) { typeMeta ->
                                parameters[name] = ParamMeta(name, typeMeta)
                            }
                        }
                    }
                }

                override fun visitEnd() {
                    if (!functionSelector.acceptFunctionParameters(parameters)) {
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
        return if (functionSelector.visitFunction(flags, CONSTRUCTOR_NAME)) {
            parameters.clear()

            object : KmConstructorVisitor() {
                override fun visitValueParameter(flags: Flags, name: String): KmValueParameterVisitor? {
                    val valueFlags = flags
                    return object : KmValueParameterVisitor() {
                        override fun visitType(flags: Flags): KmTypeVisitor? {
                            return TypeExtractorVisitor(valueFlags, flags) { typeMeta ->
                                parameters[name] = ParamMeta(name, typeMeta)
                            }
                        }
                    }
                }

                override fun visitEnd() {
                    if (!functionSelector.acceptFunctionParameters(parameters)) {
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
                    val valueFlags = flags
                    return object : KmValueParameterVisitor() {
                        override fun visitType(flags: Flags): KmTypeVisitor? {
                            return TypeExtractorVisitor(valueFlags, flags) { typeMeta ->
                                parameters[name] = ParamMeta(name, typeMeta)
                            }
                        }
                    }
                }

                override fun visitEnd() {
                    if (!functionSelector.acceptFunctionParameters(parameters)) {
                        parameters.clear()
                    }
                }
            }
        } else null
    }
}

class TypeExtractorVisitor(
    private val valueFlags: Flags,
    private val typeFlags: Flags,
    private val typeMeta: MutableList<TypeMeta> = mutableListOf(),
    private val onVisitEnd: OnVisitEnd? = null
) : KmTypeVisitor() {

    override fun visitClass(name: ClassName) {
        typeMeta.add(
            TypeMeta(
                type = name,
                nullable = Flag.Type.IS_NULLABLE(typeFlags),
                default = Flag.ValueParameter.DECLARES_DEFAULT_VALUE(valueFlags)
            )
        )
    }

    override fun visitArgument(flags: Flags, variance: KmVariance): KmTypeVisitor? =
        TypeExtractorVisitor(valueFlags, flags, typeMeta)

    override fun visitEnd() {
        onVisitEnd?.invoke(typeMeta)
    }
}

typealias OnVisitEnd = (List<TypeMeta>) -> Unit

const val CONSTRUCTOR_NAME = "<init>"