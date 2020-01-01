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
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.WildcardTypeName
import magnet.Classifier
import magnet.Instance
import magnet.Scope
import magnet.processor.MagnetProcessorEnv
import magnet.processor.common.KotlinMethodMetadata
import magnet.processor.common.eachAttributeOf
import magnet.processor.common.isOfAnnotationType
import magnet.processor.common.throwCompilationError
import magnet.processor.common.throwValidationError
import magnet.processor.instances.Cardinality
import magnet.processor.instances.Expression
import magnet.processor.instances.FactoryType
import magnet.processor.instances.MethodParameter
import magnet.processor.instances.parser.AspectValidator.Registry.VALIDATORS
import magnet.processor.instances.parser.AttributeParser.Registry.PARSERS
import javax.lang.model.element.Element
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeKind

const val FACTORY_SUFFIX = "MagnetFactory"
private const val CLASS_NULLABLE = ".Nullable"

internal abstract class InstanceParser<E : Element>(
    private val env: MagnetProcessorEnv,
    private val isTypeInheritanceEnforced: Boolean
) {

    private val scopeTypeName = ClassName.get(Scope::class.java)
    private val listTypeName = ClassName.get(List::class.java)
    private val lazyTypeName = ClassName.get(Lazy::class.java)

    fun E.parse(): List<FactoryType> {

        onBeforeParsing()

        var scope = AttributeParser.Scope(
            isTypeInheritanceEnforced = isTypeInheritanceEnforced,
            instance = ParserInstance(this),
            element = this,
            env = env
        )
        eachAttributeOf<Instance> { name, value ->
            PARSERS[name]?.apply {
                scope = scope.copy(
                    instance = scope.parse(value)
                )
            } ?: throwCompilationError(
                "Unsupported attribute '$name'." +
                    " Do you use the same versions of magnet processor and runtime libraries?"
            )
        }

        var instance = scope.instance
        for (validator in VALIDATORS) {
            with(validator) {
                instance = instance.validate()
            }
        }

        return generateFactories(instance)
    }

    protected open fun E.onBeforeParsing() {}
    protected abstract fun generateFactories(instance: ParserInstance<E>): List<FactoryType>

    protected fun parseMethodParameter(
        element: Element,
        variable: VariableElement,
        methodMeta: KotlinMethodMetadata?
    ): MethodParameter {

        val variableType = variable.asType()
        if (variableType.kind == TypeKind.TYPEVAR) {
            element.throwValidationError(
                "Constructor parameter '${variable.simpleName}' is specified using a generic" +
                    " type which is not supported by Magnet. Use a non-parameterized class or" +
                    " interface type instead. To inject current scope into an instance," +
                    " add 'scope: Scope' to the constructor parameters."
            )
        }

        val paramSpec = ParameterSpec.get(variable)
        val paramType = paramSpec.type
        val paramName = paramSpec.name

        var paramReturnType: TypeName = paramType
        var paramExpression: Expression = Expression.Scope
        var paramParameterType: TypeName = paramType
        var paramClassifier: String = Classifier.NONE
        var paramTypeErased = false

        paramParameterType.parseParamType(
            paramName, methodMeta, variable
        ) { returnType, expression, parameterType, classifier, erased ->
            paramReturnType = returnType
            paramExpression = expression
            paramParameterType = parameterType
            paramClassifier = classifier
            paramTypeErased = erased
        }

        return MethodParameter(
            name = paramName,
            expression = paramExpression,
            returnType = paramReturnType,
            parameterType = paramParameterType,
            classifier = paramClassifier,
            typeErased = paramTypeErased
        )
    }

    private fun TypeName.parseParamType(
        paramName: String,
        methodMeta: KotlinMethodMetadata?,
        variable: VariableElement,
        block: (
            returnType: TypeName,
            expression: Expression,
            parameterType: TypeName,
            classifier: String,
            typeErased: Boolean
        ) -> Unit
    ) {
        var paramReturnType: TypeName = this
        var paramExpression: Expression = Expression.Scope
        var paramParameterType: TypeName = this
        var paramClassifier: String = Classifier.NONE
        var paramTypeErased = false

        when (this) {
            scopeTypeName -> {
                paramExpression = Expression.Scope
            }

            is ParameterizedTypeName -> {
                when (rawType) {
                    listTypeName -> {
                        val (type, erased) = firstArgumentRawType(variable)
                        paramParameterType = type
                        paramTypeErased = erased
                        paramReturnType = if (erased) listTypeName
                        else ParameterizedTypeName.get(listTypeName, paramParameterType)
                        paramExpression = Expression.Getter(Cardinality.Many)
                        variable.annotations { _, classifier ->
                            paramClassifier = classifier
                        }
                    }

                    lazyTypeName -> {
                        if (methodMeta == null) variable.throwValidationError(
                            "Lazy can only be used with Kotlin classes."
                        )

                        parseLazyArgumentType(
                            paramName, methodMeta, variable
                        ) { returnType, cardinality, parameterType ->
                            paramReturnType = ParameterizedTypeName.get(lazyTypeName, returnType)
                            paramParameterType = parameterType
                            paramExpression = Expression.LazyGetter(cardinality)
                            variable.annotations { _, classifier ->
                                paramClassifier = classifier
                            }
                        }
                    }

                    else -> {
                        paramParameterType = rawType
                        paramTypeErased = true
                        variable.annotations { cardinality, classifier ->
                            paramExpression = Expression.Getter(cardinality)
                            paramClassifier = classifier
                        }
                    }
                }
            }

            else -> {
                variable.annotations { cardinality, classifier ->
                    paramExpression = Expression.Getter(cardinality)
                    paramClassifier = classifier
                }
            }
        }

        block(
            paramReturnType,
            paramExpression,
            paramParameterType,
            paramClassifier,
            paramTypeErased
        )
    }

    private fun ParameterizedTypeName.parseLazyArgumentType(
        paramName: String,
        methodMeta: KotlinMethodMetadata,
        variable: VariableElement,
        block: (
            returnType: TypeName,
            cardinality: Cardinality,
            parameterType: TypeName
        ) -> Unit
    ) {
        when (val argumentType = typeArguments.first().withoutWildcards(variable)) {
            scopeTypeName -> variable.throwValidationError("Lazy cannot be parametrized with Scope type.")
            is ParameterizedTypeName -> {
                when (argumentType.rawType) {
                    lazyTypeName -> variable.throwValidationError("Lazy cannot be parametrized with another Lazy type.")
                    listTypeName -> {
                        if (methodMeta.getTypeMeta(paramName, 1).nullable) {
                            variable.throwValidationError(
                                "Lazy<List> must be parametrized with none nullable List type."
                            )
                        }
                        when (val listArgumentType = argumentType.typeArguments.first().withoutWildcards(variable)) {
                            is ParameterizedTypeName -> {
                                block(
                                    ParameterizedTypeName.get(listTypeName, listArgumentType),
                                    Cardinality.Many,
                                    listArgumentType.rawType
                                )
                            }
                            else -> {
                                if (methodMeta.getTypeMeta(paramName, 2).nullable) {
                                    variable.throwValidationError(
                                        "Lazy<List<T>> must be parametrized with none nullable type."
                                    )
                                }
                                block(
                                    ParameterizedTypeName.get(listTypeName, listArgumentType),
                                    Cardinality.Many,
                                    listArgumentType
                                )
                            }
                        }
                    }
                    else -> {
                        block(
                            argumentType,
                            methodMeta.getNullableCardinality(paramName, 1),
                            argumentType.rawType
                        )
                    }
                }
            }
            else -> {
                block(
                    argumentType,
                    methodMeta.getNullableCardinality(paramName, 1),
                    argumentType
                )
            }
        }
    }
}

private fun KotlinMethodMetadata.getNullableCardinality(paramName: String, paramDepth: Int): Cardinality =
    if (getTypeMeta(paramName, paramDepth).nullable) Cardinality.Optional
    else Cardinality.Single

private fun TypeName.withoutWildcards(element: Element): TypeName =
    if (this is WildcardTypeName) {
        checkBounds(element)
        upperBounds.first()
    } else this

private fun WildcardTypeName.firstUpperBoundsRawType(element: Element): Pair<TypeName, Boolean> {
    checkBounds(element)
    return when (val type = upperBounds.first()) {
        is ParameterizedTypeName -> type.rawType to true
        is WildcardTypeName -> type.firstUpperBoundsRawType(element)
        else -> type to false
    }
}

private fun WildcardTypeName.checkBounds(element: Element) {
    if (lowerBounds.size > 0) {
        element.throwValidationError(
            "Magnet supports single upper bounds class parameter only," +
                " while lower bounds class parameter was found."
        )
    }

    if (upperBounds.size > 1) {
        element.throwValidationError(
            "Magnet supports single upper bounds class parameter only," +
                " for example List<${upperBounds.first()}>"
        )
    }
}

private fun ParameterizedTypeName.firstArgumentRawType(element: Element): Pair<TypeName, Boolean> {
    if (typeArguments.size > 1) {
        element.throwValidationError("Magnet supports type parametrized with a single argument only.")
    }
    return when (val argumentType = typeArguments.first()) {
        is ParameterizedTypeName -> argumentType.rawType to true
        is WildcardTypeName -> argumentType.firstUpperBoundsRawType(element)
        else -> argumentType to false
    }
}

private inline fun VariableElement.annotations(block: (Cardinality, String) -> Unit) {
    var cardinality = Cardinality.Single
    var classifier = Classifier.NONE
    annotationMirrors.forEach { annotationMirror ->
        if (annotationMirror.isOfAnnotationType<Classifier>()) {
            val declaredClassifier: String? = annotationMirror.elementValues.values.firstOrNull()?.value.toString()
            declaredClassifier?.let {
                classifier = it.removeSurrounding("\"", "\"")
            }
        } else {
            val annotationType = annotationMirror.annotationType.toString()
            if (annotationType.endsWith(CLASS_NULLABLE)) {
                cardinality = Cardinality.Optional
            }
        }
    }
    block(cardinality, classifier)
}
