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

package magnet.processor.factory.generator

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import magnet.Classifier
import magnet.Scope
import magnet.Scoping
import magnet.internal.InstanceFactory
import magnet.processor.factory.CodeGenerator
import magnet.processor.factory.CodeWriter
import magnet.processor.factory.CreateMethod
import magnet.processor.factory.FactoryType
import magnet.processor.factory.FactoryTypeVisitor
import magnet.processor.factory.GetScopingMethod
import magnet.processor.factory.GetSiblingTypesMethod
import magnet.processor.factory.GetterMethod
import magnet.processor.factory.MethodCreateStatement
import magnet.processor.factory.MethodParameter
import magnet.processor.factory.PARAM_SCOPE_NAME
import magnet.processor.factory.TypeCreateStatement
import javax.lang.model.element.Modifier

interface Generator {
    fun generate(classBuilder: TypeSpec.Builder)
    fun reset()
}

internal class GenerationAspect<out G : Generator>(
    private val generator: G
) {
    private var visited: Boolean = false

    inline fun visit(block: G.() -> Unit) {
        block(generator)
        visited = true
    }

    fun generate(classBuilder: TypeSpec.Builder) {
        if (visited) {
            generator.generate(classBuilder)
        }
        generator.reset()
        visited = false
    }
}

class FactoryCodeGenerator : FactoryTypeVisitor, CodeGenerator {

    private var factoryTypeSpec: TypeSpec? = null
    private var factoryClassName: ClassName? = null

    private lateinit var createMethodCodeBuilder: CodeBlock.Builder
    private var shouldSuppressUncheckedWarning = false
    private var constructorParametersBuilder = StringBuilder()
    private var getScoping: MethodSpec? = null

    private var aspectGetSiblingTypes = GenerationAspect(GetSiblingTypesMethodGenerator())

    override fun visitEnter(factoryType: FactoryType) {}

    override fun visitEnter(createMethod: CreateMethod) {
        shouldSuppressUncheckedWarning = false
        factoryTypeSpec = null
        getScoping = null
        createMethodCodeBuilder = CodeBlock.builder()
        constructorParametersBuilder.setLength(0)
    }

    override fun visit(parameter: MethodParameter) {
        val isScopeParameter = parameter.name == PARAM_SCOPE_NAME

        if (!isScopeParameter) {
            if (parameter.classifier == Classifier.NONE) {
                if (parameter.method == GetterMethod.GET_MANY) {
                    if (parameter.typeErased) {
                        createMethodCodeBuilder.addStatement(
                            "\$T ${parameter.name} = scope.${parameter.method.code}(\$T.class)",
                            List::class.java,
                            parameter.type
                        )
                    } else {
                        createMethodCodeBuilder.addStatement(
                            "\$T<\$T> ${parameter.name} = scope.${parameter.method.code}(\$T.class)",
                            List::class.java,
                            parameter.type,
                            parameter.type
                        )
                    }

                } else {
                    createMethodCodeBuilder.addStatement(
                        "\$T ${parameter.name} = scope.${parameter.method.code}(\$T.class)",
                        parameter.type,
                        parameter.type
                    )
                }

            } else {
                if (parameter.method == GetterMethod.GET_MANY) {
                    if (parameter.typeErased) {
                        createMethodCodeBuilder.addStatement(
                            "\$T ${parameter.name} = scope.${parameter.method.code}(\$T.class, \$S)",
                            List::class.java,
                            parameter.type,
                            parameter.classifier
                        )
                    } else {
                        createMethodCodeBuilder.addStatement(
                            "\$T<\$T> ${parameter.name} = scope.${parameter.method.code}(\$T.class, \$S)",
                            List::class.java,
                            parameter.type,
                            parameter.type,
                            parameter.classifier
                        )
                    }
                } else {
                    createMethodCodeBuilder.addStatement(
                        "\$T ${parameter.name} = scope.${parameter.method.code}(\$T.class, \$S)",
                        parameter.type,
                        parameter.type,
                        parameter.classifier
                    )
                }
            }
        }

        if (parameter.typeErased) {
            shouldSuppressUncheckedWarning = true
        }

        constructorParametersBuilder.append(parameter.name).append(", ")
    }

    override fun visitExit(createMethod: CreateMethod) {
        if (constructorParametersBuilder.isNotEmpty()) {
            constructorParametersBuilder.setLength(constructorParametersBuilder.length - 2)
        }
    }

    override fun visit(method: GetScopingMethod) {
        getScoping = MethodSpec
            .methodBuilder("getScoping")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override::class.java)
            .returns(Scoping::class.java)
            .addStatement("return \$T.\$L", Scoping::class.java, method.scoping)
            .build()
    }

    override fun enterSiblingTypesMethod(method: GetSiblingTypesMethod) {
        aspectGetSiblingTypes.visit { enterSiblingTypesMethod(method) }
    }

    override fun visitSiblingType(type: ClassName) {
        aspectGetSiblingTypes.visit { visitSiblingType(type) }
    }

    override fun exitSiblingTypesMethod(method: GetSiblingTypesMethod) {
        aspectGetSiblingTypes.visit { exitSiblingTypesMethod() }
    }

    override fun visitExit(factory: FactoryType) {
        factoryClassName = factory.factoryType

        val classBuilder = TypeSpec
            .classBuilder(factoryClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .superclass(generateFactorySuperInterface(factory))
            .addMethod(generateCreateMethod(factory))
            .addMethod(generateGetScopingMethod())

        aspectGetSiblingTypes.generate(classBuilder)

        classBuilder
            .addMethod(generateGetTypeMethod(factory))

        factoryTypeSpec = classBuilder.build()
    }

    private fun generateCreateMethod(factoryType: FactoryType): MethodSpec {
        var builder = MethodSpec
            .methodBuilder("create")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(
                ParameterSpec
                    .builder(Scope::class.java, PARAM_SCOPE_NAME)
                    .build()
            )
            .returns(factoryType.type)
            .addCode(createMethodCodeBuilder.build())

        if (shouldSuppressUncheckedWarning) {
            builder = builder.addAnnotation(
                AnnotationSpec
                    .builder(SuppressWarnings::class.java)
                    .addMember("value", "\"unchecked\"")
                    .build()
            )
        }

        val createStatement = factoryType.createStatement
        when (createStatement) {
            is TypeCreateStatement -> {
                builder.addStatement(
                    "return new \$T($constructorParametersBuilder)",
                    createStatement.instanceType
                )
            }
            is MethodCreateStatement -> {
                builder.addStatement(
                    "return \$T.\$L($constructorParametersBuilder)",
                    createStatement.staticMethodClassName,
                    createStatement.staticMethodName
                )
            }
        }

        return builder.build()
    }

    private fun generateGetScopingMethod(): MethodSpec {
        return getScoping!!
    }

    private fun generateGetTypeMethod(factoryType: FactoryType): MethodSpec {
        return MethodSpec
            .methodBuilder("getType")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(Class::class.java)
            .addStatement("return \$T.class", factoryType.type)
            .build()
    }

    private fun generateFactorySuperInterface(factoryType: FactoryType): TypeName {
        return ParameterizedTypeName.get(
            ClassName.get(InstanceFactory::class.java),
            factoryType.type
        )
    }

    override fun generateFrom(factoryType: FactoryType): CodeWriter {
        factoryType.accept(this)
        return CodeWriter(this.factoryClassName!!.packageName(), factoryTypeSpec!!)
    }

}
