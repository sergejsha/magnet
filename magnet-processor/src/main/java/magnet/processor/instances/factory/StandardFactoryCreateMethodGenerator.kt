package magnet.processor.instances.factory

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import magnet.Scope
import magnet.processor.instances.CreateMethod
import magnet.processor.instances.FactoryType
import magnet.processor.instances.MethodParameter
import magnet.processor.instances.PARAM_SCOPE_NAME
import javax.lang.model.element.Modifier

class StandardFactoryCreateMethodGenerator : CreateMethodGenerator {

    private lateinit var factoryType: FactoryType
    private var createMethodBuilder: MethodSpec.Builder? = null
    private var createMethodCodeBuilder: CodeBlock.Builder? = null
    private var constructorParametersBuilder = StringBuilder()
    private var isSuppressUncheckedAdded = false

    override fun visitFactoryClass(factoryType: FactoryType) {
        this.factoryType = factoryType
        createMethodBuilder = null
        createMethodCodeBuilder = null
        constructorParametersBuilder.setLength(0)
        isSuppressUncheckedAdded = false
    }

    override fun enterCreateMethod(createMethod: CreateMethod) {
        createMethodBuilder = MethodSpec
            .methodBuilder("create")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(Scope::class.java, PARAM_SCOPE_NAME)
            .returns(factoryType.interfaceType)
        createMethodCodeBuilder = CodeBlock.builder()
    }

    override fun visitCreateMethodParameter(parameter: MethodParameter) {
        createMethodCodeBuilder?.let { builder ->
            builder.addCreateParameterStatement(parameter)
            constructorParametersBuilder.append(parameter.name).append(", ")
        }

        createMethodBuilder?.let { builder ->
            if (parameter.typeErased && !isSuppressUncheckedAdded) {
                isSuppressUncheckedAdded = true
                builder.addAnnotation(
                    AnnotationSpec
                        .builder(SuppressWarnings::class.java)
                        .addMember("value", "\"unchecked\"")
                        .build()
                )
            }
        }

    }

    override fun exitCreateMethod() {
        if (constructorParametersBuilder.isNotEmpty()) {
            constructorParametersBuilder.setLength(constructorParametersBuilder.length - 2)
        }
    }

    override fun generate(typeBuilder: TypeSpec.Builder) {
        createMethodBuilder?.let { builder ->

            createMethodCodeBuilder?.let { codeBuilder ->
                builder.addCode(codeBuilder.build())
            }

            builder.addNewInstanceStatement(
                constructorParametersBuilder.toString(),
                factoryType.createStatement
            )

            typeBuilder.addMethod(builder.build())
        }
    }

}

