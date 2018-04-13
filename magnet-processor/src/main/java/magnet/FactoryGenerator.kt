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

package magnet

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.WildcardTypeName
import magnet.processor.model.FactoryCodeGenerator
import magnet.processor.model.FactoryFromTypeParser
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.lang.model.util.ElementFilter

private const val CLASS_NULLABLE = ".Nullable"
private const val PARAM_SCOPE = "scope"
private const val METHOD_GET_OPTIONAL = "getOptional"
private const val METHOD_GET_SINGLE = "getSingle"
private const val METHOD_GET_MANY = "getMany"
private const val INSTANCE_RETENTION = "instanceRetention"

class FactoryGenerator {

    private lateinit var env: MagnetProcessorEnv

    fun generate(element: ExecutableElement, env: MagnetProcessorEnv) {
        // todo
    }

    fun generate(element: TypeElement, env: MagnetProcessorEnv) {
        val parser = FactoryFromTypeParser(env)
        val factoryType = parser.parse(element)
        val generator = FactoryCodeGenerator()
        factoryType.accept(generator)

        JavaFile.builder(factoryType.factoryType.packageName(), generator.getTypeSpec())
            .skipJavaLangImports(true)
            .build()
            .writeTo(env.filer)
    }

    fun generate2(implTypeElement: TypeElement, env: MagnetProcessorEnv) {
        this.env = env
        val implClassName = ClassName.get(implTypeElement)

        implTypeElement.annotationMirrors.forEach {
            if (it.mirrors<Implementation>()) {
                var annotationValueType: String? = null
                var instanceRetention = InstanceRetention.SCOPE.name

                it.elementValues.entries.forEach {
                    val valueName = it.key.simpleName.toString()
                    val value = it.value.value.toString()

                    if (valueName == "type") {
                        annotationValueType = value

                    } else if (valueName == INSTANCE_RETENTION) {
                        instanceRetention = value
                    }
                }

                val implType = env.elements.getTypeElement(annotationValueType)
                val isTypeImplemented = env.types.isAssignable(
                    implTypeElement.asType(),
                    env.types.getDeclaredType(implType) // we deliberately erase generic type here
                )
                if (!isTypeImplemented) {
                    env.reportError(implTypeElement, "$implTypeElement must implement $implType")
                    throw BreakGenerationException()
                }

                val implTypeClassName = ClassName.get(implType)
                val factoryTypeSpec = generateFactory(
                    implClassName,
                    implTypeClassName,
                    implTypeElement,
                    instanceRetention
                )

                val packageName = implClassName.packageName()
                JavaFile.builder(packageName, factoryTypeSpec)
                    .skipJavaLangImports(true)
                    .build()
                    .writeTo(env.filer)
            }
        }
    }

    private fun generateFactory(
        implClassName: ClassName,
        implTypeClassName: ClassName,
        implTypeElement: TypeElement,
        instanceRetention: String
    ): TypeSpec {

        val factoryPackage = implClassName.packageName()
        val factoryName = "Magnet${implClassName.simpleName()}Factory"
        val factoryClassName = ClassName.bestGuess("$factoryPackage.$factoryName")

        val extensionFactorySuperInterface = ParameterizedTypeName.get(
            ClassName.get(InstanceFactory::class.java),
            implTypeClassName
        )

        return TypeSpec
            .classBuilder(factoryClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(extensionFactorySuperInterface)
            .addMethod(
                generateCreateMethod(
                    implClassName,
                    implTypeClassName,
                    implTypeElement
                )
            )
            .addMethod(
                generateGetInstanceRetentionMethod(
                    instanceRetention
                )
            )
            .addMethod(
                generateGetTypeMethod(
                    implTypeClassName
                )
            )
            .build()
    }

    private fun generateGetInstanceRetentionMethod(
        instanceRetention: String
    ): MethodSpec {
        return MethodSpec
            .methodBuilder("getInstanceRetention")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override::class.java)
            .returns(InstanceRetention::class.java)
            .addStatement("return \$T.\$L", InstanceRetention::class.java, instanceRetention)
            .build()
    }

    private fun generateGetTypeMethod(
        implTypeClassName: ClassName
    ): MethodSpec {
        return MethodSpec
            .methodBuilder("getType")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(Class::class.java)
            .addStatement("return \$T.class", implTypeClassName)
            .build()
    }

    private fun generateCreateMethod(
        implClassName: ClassName,
        implTypeClassName: ClassName,
        implTypeElement: TypeElement
    ): MethodSpec {
        val scopeClassName = ClassName.get(Scope::class.java)

        val constructors = ElementFilter.constructorsIn(implTypeElement.enclosedElements)
        if (constructors.size != 1) {
            env.reportError(implTypeElement, "Exactly one constructor is required for $implTypeElement")
            throw BreakGenerationException()
        }

        val parameters = constructors[0].parameters
        val codeBlockBuilder = CodeBlock.builder()
        val methodParamsBuilder = StringBuilder()

        parameters.forEach {
            val type = it.asType()

            if (type.kind == TypeKind.TYPEVAR) {
                env.reportError(implTypeElement,
                    "Constructor parameter '${it.simpleName}' is specified using a generic type which" +
                        " is an invalid parameter type. Use a class or an interface type instead." +
                        " 'Scope' is a valid parameter type too.")
                throw BreakGenerationException()
            }

            val isScopeParam = type.toString() == Scope::class.java.name
            val paramName = if (isScopeParam) PARAM_SCOPE else it.simpleName.toString()

            if (!isScopeParam) {
                val paramType = ParameterSpec.get(it).type
                var getMethodName: String? = null
                var paramClassName =
                    if (paramType is ParameterizedTypeName) {
                        if (paramType.rawType.reflectionName() == List::class.java.typeName) {
                            getMethodName = METHOD_GET_MANY
                            paramType.typeArguments[0]
                        } else {
                            paramType.rawType
                        }
                    } else {
                        ClassName.get(type)
                    }

                if (paramClassName is WildcardTypeName) {

                    if (paramClassName.lowerBounds.size > 0) {
                        env.reportError(implTypeElement,
                            "Only single upper bounds class parameter is supported," +
                                " for example List<${paramClassName.lowerBounds[0]}>")
                        throw BreakGenerationException()
                    }

                    val upperBounds = paramClassName.upperBounds
                    if (upperBounds.size > 1) {
                        env.reportError(implTypeElement,
                            "Only single upper bounds class parameter is supported," +
                                " for example List<${upperBounds[0]}>")
                        throw BreakGenerationException()
                    }

                    paramClassName = upperBounds[0]
                }

                var hasNullableAnnotation = false
                var namedAnnotationValue: String? = null

                it.annotationMirrors.forEach { annotationMirror ->

                    if (annotationMirror.mirrors<Classifier>()) {
                        namedAnnotationValue = annotationMirror.elementValues.values.firstOrNull()?.value.toString()
                        namedAnnotationValue?.removeSurrounding("\"", "\"")

                    } else {
                        val annotationType = annotationMirror.annotationType.toString()
                        if (annotationType.endsWith(CLASS_NULLABLE)) {
                            hasNullableAnnotation = true
                        }
                    }
                }

                if (getMethodName == null) {
                    getMethodName = if (hasNullableAnnotation) METHOD_GET_OPTIONAL else METHOD_GET_SINGLE
                }

                if (namedAnnotationValue != null) {
                    if (getMethodName == METHOD_GET_MANY) {
                        codeBlockBuilder.addStatement(
                            "\$T<\$T> $paramName = scope.$getMethodName(\$T.class, \$S)",
                            List::class.java,
                            paramClassName,
                            paramClassName,
                            namedAnnotationValue
                        )

                    } else {
                        codeBlockBuilder.addStatement(
                            "\$T $paramName = scope.$getMethodName(\$T.class, \$S)",
                            paramClassName,
                            paramClassName,
                            namedAnnotationValue
                        )
                    }
                } else {
                    if (getMethodName == METHOD_GET_MANY) {
                        codeBlockBuilder.addStatement(
                            "\$T<\$T> $paramName = scope.$getMethodName(\$T.class)",
                            List::class.java,
                            paramClassName,
                            paramClassName
                        )

                    } else {
                        codeBlockBuilder.addStatement(
                            "\$T $paramName = scope.$getMethodName(\$T.class)",
                            paramClassName,
                            paramClassName
                        )
                    }
                }

            }

            methodParamsBuilder.append(paramName).append(", ")
        }

        if (methodParamsBuilder.isNotEmpty()) {
            methodParamsBuilder.setLength(methodParamsBuilder.length - 2)
        }

        return MethodSpec
            .methodBuilder("create")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ParameterSpec
                .builder(scopeClassName, PARAM_SCOPE)
                .build())
            .returns(implTypeClassName)
            .addCode(codeBlockBuilder.build())
            .addStatement("return new \$T($methodParamsBuilder)", implClassName)
            .build()
    }

}
