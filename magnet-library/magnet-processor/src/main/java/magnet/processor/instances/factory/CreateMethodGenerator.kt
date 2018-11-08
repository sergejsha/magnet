package magnet.processor.instances.factory

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import magnet.Classifier
import magnet.processor.instances.CreateMethod
import magnet.processor.instances.CreateStatement
import magnet.processor.instances.FactoryType
import magnet.processor.instances.GetterMethod
import magnet.processor.instances.MethodParameter
import magnet.processor.instances.PARAM_SCOPE_NAME
import magnet.processor.instances.StaticMethodCreateStatement
import magnet.processor.instances.TypeCreateStatement

interface CreateMethodGenerator {

    fun visitFactoryClass(factoryType: FactoryType) {}
    fun enterCreateMethod(createMethod: CreateMethod) {}
    fun visitCreateMethodParameter(parameter: MethodParameter) {}
    fun exitCreateMethod() {}
    fun generate(typeBuilder: TypeSpec.Builder)

    fun CodeBlock.Builder.addCreateParameterStatement(parameter: MethodParameter) {
        val isScopeParameter = parameter.name == PARAM_SCOPE_NAME
        if (!isScopeParameter) {
            if (parameter.classifier == Classifier.NONE) {
                if (parameter.method == GetterMethod.GET_MANY) {
                    if (parameter.typeErased) {
                        addStatement(
                            "\$T ${parameter.name} = scope.${parameter.method.code}(\$T.class)",
                            List::class.java,
                            parameter.type
                        )
                    } else {
                        addStatement(
                            "\$T<\$T> ${parameter.name} = scope.${parameter.method.code}(\$T.class)",
                            List::class.java,
                            parameter.type,
                            parameter.type
                        )
                    }

                } else {
                    addStatement(
                        "\$T ${parameter.name} = scope.${parameter.method.code}(\$T.class)",
                        parameter.type,
                        parameter.type
                    )
                }

            } else {
                if (parameter.method == GetterMethod.GET_MANY) {
                    if (parameter.typeErased) {
                        addStatement(
                            "\$T ${parameter.name} = scope.${parameter.method.code}(\$T.class, \$S)",
                            List::class.java,
                            parameter.type,
                            parameter.classifier
                        )
                    } else {
                        addStatement(
                            "\$T<\$T> ${parameter.name} = scope.${parameter.method.code}(\$T.class, \$S)",
                            List::class.java,
                            parameter.type,
                            parameter.type,
                            parameter.classifier
                        )
                    }
                } else {
                    addStatement(
                        "\$T ${parameter.name} = scope.${parameter.method.code}(\$T.class, \$S)",
                        parameter.type,
                        parameter.type,
                        parameter.classifier
                    )
                }
            }
        }
    }

    fun MethodSpec.Builder.addNewInstanceStatement(
        constructorParameters: String,
        createStatement: CreateStatement
    ): MethodSpec.Builder {
        when (createStatement) {
            is TypeCreateStatement -> {
                addStatement(
                    "return new \$T($constructorParameters)",
                    createStatement.instanceType
                )
            }
            is StaticMethodCreateStatement -> {
                addStatement(
                    "return \$T.\$L($constructorParameters)",
                    createStatement.staticMethodClassName,
                    createStatement.staticMethodName
                )
            }
        }
        return this
    }

}

class DefaultCreateMethodGenerator : CreateMethodGenerator {

    private val customFactoryGenerator = CustomFactoryCreateMethodGenerator()
    private val standardFactoryGenerator = StandardFactoryCreateMethodGenerator()

    private lateinit var impl: CreateMethodGenerator

    override fun visitFactoryClass(factoryType: FactoryType) {
        impl = if (factoryType.customFactoryType != null) customFactoryGenerator else standardFactoryGenerator
        impl.visitFactoryClass(factoryType)
    }

    override fun enterCreateMethod(createMethod: CreateMethod) {
        impl.enterCreateMethod(createMethod)
    }

    override fun visitCreateMethodParameter(parameter: MethodParameter) {
        impl.visitCreateMethodParameter(parameter)
    }

    override fun exitCreateMethod() {
        impl.exitCreateMethod()
    }

    override fun generate(typeBuilder: TypeSpec.Builder) {
        impl.generate(typeBuilder)
    }

}
