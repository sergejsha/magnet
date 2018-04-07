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
import magnet.internal.Factory
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.lang.model.util.ElementFilter

private const val CLASS_JAVAX_NAMED = "javax.inject.Named"
private const val CLASS_NULLABLE = ".Nullable"

class FactoryGenerator {

    private lateinit var env: MagnetProcessorEnv

    fun generate(implTypeElement: TypeElement, env: MagnetProcessorEnv) {
        this.env = env
        val implClassName = ClassName.get(implTypeElement)

        implTypeElement.annotationMirrors.forEach {
            val extensionClass = it.elementValues.entries.find { "type" == it.key.simpleName.toString() }?.value

            extensionClass?.let {
                val implType = env.elements.getTypeElement(it.value.toString())
                val isTypeImplemented = env.types.isAssignable(
                    implTypeElement.asType(),
                    env.types.getDeclaredType(implType) // we deliberately erase generic type here
                )
                if (!isTypeImplemented) {
                    env.reportError(implTypeElement, "$implTypeElement must implement $implType")
                    throw BreakGenerationException()
                }

                val implTypeClassName = ClassName.get(implType)
                val factoryTypeSpec = generateFactory(implClassName, implTypeClassName, implTypeElement)

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
        implTypeElement: TypeElement
    ): TypeSpec {

        val factoryPackage = implClassName.packageName()
        val factoryName = "Magnet${implClassName.simpleName()}Factory"
        val factoryClassName = ClassName.bestGuess("$factoryPackage.$factoryName")

        val extensionFactorySuperInterface = ParameterizedTypeName.get(
            ClassName.get(Factory::class.java),
            implTypeClassName)

        return TypeSpec
            .classBuilder(factoryClassName)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(extensionFactorySuperInterface)
            //.addField(generateComponentRegistryField())
            //.addMethod(generateConstructor())
            .addMethod(
                generateCreateMethod(
                    implClassName,
                    implTypeClassName,
                    implTypeElement
                )
            )
            .addMethod(
                generateGetTypeMethod(
                    implTypeClassName
                )
            )
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
        val dependencyScopeClassName = ClassName.get(DependencyScope::class.java)

        // We have following cases:
        // 1. No parameters -> empty constructor
        // 2. One or many parameters -> DependencyScope used "as is" others are required() from scope

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
                        " 'DependencyScope' is a valid parameter type too.")
                throw BreakGenerationException()
            }

            val isDependencyScopeParam = type.toString() == DependencyScope::class.java.name
            val paramName = if (isDependencyScopeParam) "dependencyScope" else it.simpleName.toString()

            if (!isDependencyScopeParam) {
                val paramClassName = ClassName.get(type)

                var hasNullableAnnotation = false
                var namedAnnotationValue: String? = null

                it.annotationMirrors.forEach { annotation ->
                    val annotationType = annotation.annotationType.toString()
                    if (annotationType.endsWith(CLASS_NULLABLE)) {
                        hasNullableAnnotation = true

                    } else if (annotationType == CLASS_JAVAX_NAMED) {
                        namedAnnotationValue = annotation.elementValues.values.firstOrNull()?.value.toString()
                        namedAnnotationValue?.removeSurrounding("\"", "\"")
                    }
                }

                val getMethodName = if (hasNullableAnnotation) "get" else "require"

                if (namedAnnotationValue != null) {
                    codeBlockBuilder.addStatement(
                        "\$T $paramName = dependencyScope.$getMethodName(\$T.class, \$S)",
                        paramClassName,
                        paramClassName,
                        namedAnnotationValue
                    )
                } else {
                    codeBlockBuilder.addStatement(
                        "\$T $paramName = dependencyScope.$getMethodName(\$T.class)",
                        paramClassName,
                        paramClassName
                    )
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
                .builder(dependencyScopeClassName, "dependencyScope")
                .build())
            .returns(implTypeClassName)
            .addCode(codeBlockBuilder.build())
            .addStatement("return new \$T($methodParamsBuilder)", implClassName)
            .build()
    }

}
