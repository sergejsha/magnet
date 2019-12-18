package magnet.processor.instances.aspects.factory

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import magnet.internal.ManyLazy
import magnet.internal.OptionalLazy
import magnet.internal.SingleLazy
import magnet.processor.instances.Cardinality
import magnet.processor.instances.CreateMethod
import magnet.processor.instances.CreateStatement
import magnet.processor.instances.Expression
import magnet.processor.instances.FactoryType
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
        when (val expression = parameter.expression) {
            is Expression.Getter -> {
                addStatement(
                    "\$T \$L = scope.${expression.getterName}(\$T.class, \$S)",
                    parameter.returnType,
                    parameter.name,
                    parameter.parameterType,
                    parameter.classifier
                )
            }
            is Expression.LazyGetter -> {
                addStatement(
                    "\$T \$L = new \$T($PARAM_SCOPE_NAME, \$T.class, \$S)",
                    parameter.returnType,
                    parameter.name,
                    expression.lazyGetterType,
                    parameter.parameterType,
                    parameter.classifier
                )
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

private val Expression.Getter.getterName: String
    get() = when (cardinality) {
        Cardinality.Single -> "getSingle"
        Cardinality.Optional -> "getOptional"
        Cardinality.Many -> "getMany"
    }

private val Expression.LazyGetter.lazyGetterType: TypeName
    get() = when (cardinality) {
        Cardinality.Single -> ClassName.get(SingleLazy::class.java)
        Cardinality.Optional -> ClassName.get(OptionalLazy::class.java)
        Cardinality.Many -> ClassName.get(ManyLazy::class.java)
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
