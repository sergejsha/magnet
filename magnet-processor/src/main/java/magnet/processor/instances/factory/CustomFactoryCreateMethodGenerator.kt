package magnet.processor.instances.factory;

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import magnet.Factory
import magnet.Scope
import magnet.Scoping
import magnet.processor.instances.CreateMethod
import magnet.processor.instances.FactoryType
import magnet.processor.instances.MethodParameter
import javax.lang.model.element.Modifier

class CustomFactoryCreateMethodGenerator : CreateMethodGenerator {

    private lateinit var factoryType: FactoryType
    private lateinit var factoryFieldType: TypeName
    private lateinit var instantiateMethodBuilder: MethodSpec.Builder
    private lateinit var instantiateMethodCodeBuilder: CodeBlock.Builder
    private var constructorParametersBuilder = StringBuilder()

    override fun visitFactoryClass(factoryType: FactoryType) {
        this.factoryType = factoryType
        constructorParametersBuilder.setLength(0)

        val customFactoryType = checkNotNull(factoryType.customFactoryType)
        factoryFieldType = when (customFactoryType) {
            is ParameterizedTypeName -> ParameterizedTypeName.get(customFactoryType.rawType, factoryType.interfaceType)
            else -> customFactoryType
        }
    }

    override fun enterCreateMethod(createMethod: CreateMethod) {
        instantiateMethodBuilder = MethodSpec
            .methodBuilder("instantiate")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(Scope::class.java, "scope")
            .returns(factoryType.interfaceType)

        instantiateMethodCodeBuilder = CodeBlock
            .builder()
    }


    override fun visitCreateMethodParameter(parameter: MethodParameter) {
        instantiateMethodCodeBuilder.addCreateParameterStatement(parameter)
        constructorParametersBuilder.append(parameter.name).append(", ")
    }

    override fun exitCreateMethod() {
        if (constructorParametersBuilder.isNotEmpty()) {
            constructorParametersBuilder.setLength(constructorParametersBuilder.length - 2)
        }
    }

    override fun generate(typeBuilder: TypeSpec.Builder) {
        typeBuilder
            .addSuperinterface(
                ParameterizedTypeName.get(
                    ClassName.get(Factory.Instantiator::class.java),
                    factoryType.interfaceType
                )
            )
            .addField(
                FieldSpec
                    .builder(factoryFieldType, "factory")
                    .addModifiers(Modifier.PRIVATE)
                    .initializer("null")
                    .build()
            )
            .addMethod(
                MethodSpec
                    .methodBuilder("create")
                    .addAnnotation(Override::class.java)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(Scope::class.java, "scope")
                    .returns(factoryType.interfaceType)
                    .addCode(
                        CodeBlock.builder()
                            .beginControlFlow("if (factory == null)")
                            .addStatement("factory = new \$T()", factoryFieldType)
                            .endControlFlow()
                            .addStatement(
                                "return factory.create(scope, \$T.class, \$S, \$T.\$L, this)",
                                factoryType.interfaceType,
                                factoryType.classifier,
                                Scoping::class.java,
                                factoryType.scoping
                            )
                            .build()
                    )
                    .build()
            )
            .addMethod(
                instantiateMethodBuilder
                    .addCode(instantiateMethodCodeBuilder.build())
                    .addNewInstanceStatement(
                        constructorParametersBuilder.toString(),
                        factoryType.createStatement
                    )
                    .build()
            )
    }

}
