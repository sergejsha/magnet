package magnet.processor.model

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import magnet.Classifier
import magnet.InstanceFactory
import magnet.InstanceRetention
import magnet.Scope
import javax.lang.model.element.Modifier

class FactoryCodeGenerator : FactoryTypeVisitor {

    private var typeBuilder: TypeSpec.Builder? = null
    private var createMethodCodeBuilder: CodeBlock.Builder? = null
    private var constructorParametersBuilder = StringBuilder()
    private var factoryTypeSpec: TypeSpec? = null
    private var getInstanceRetention: MethodSpec? = null

    override fun visitEnter(factoryType: FactoryType) {
        typeBuilder = TypeSpec
            .classBuilder(factoryType.factoryType)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(
                ParameterizedTypeName.get(
                    ClassName.get(InstanceFactory::class.java),
                    factoryType.interfaceType
                )
            )
    }

    override fun visitEnter(createMethod: CreateMethod) {
        createMethodCodeBuilder = CodeBlock.builder()
        constructorParametersBuilder.setLength(0)
    }

    override fun visit(parameter: MethodParameter) {
        val isScopeParameter = parameter.name == PARAM_SCOPE_NAME

        if (!isScopeParameter) {
            if (parameter.classifier == Classifier.NONE) {
                if (parameter.method == GetterMethod.GET_MANY) {
                    createMethodCodeBuilder!!.addStatement(
                        "\$T<\$T> ${parameter.name} = scope.${parameter.method.code}(\$T.class)",
                        List::class.java,
                        parameter.type,
                        parameter.type
                    )

                } else {
                    createMethodCodeBuilder!!.addStatement(
                        "\$T ${parameter.name} = scope.${parameter.method.code}(\$T.class)",
                        parameter.type,
                        parameter.type
                    )
                }

            } else {
                if (parameter.method == GetterMethod.GET_MANY) {
                    createMethodCodeBuilder!!.addStatement(
                        "\$T<\$T> ${parameter.name} = scope.${parameter.method.code}(\$T.class, \$S)",
                        List::class.java,
                        parameter.type,
                        parameter.type,
                        parameter.classifier
                    )
                } else {
                    createMethodCodeBuilder!!.addStatement(
                        "\$T ${parameter.name} = scope.${parameter.method.code}(\$T.class, \$S)",
                        parameter.type,
                        parameter.type,
                        parameter.classifier
                    )
                }
            }
        }

        constructorParametersBuilder.append(parameter.name).append(", ")
    }

    override fun visitExit(createMethod: CreateMethod) {
        if (constructorParametersBuilder.isNotEmpty()) {
            constructorParametersBuilder.setLength(constructorParametersBuilder.length - 2)
        }
    }

    override fun visit(method: GetRetentionMethod) {
        getInstanceRetention = MethodSpec
            .methodBuilder("getInstanceRetention")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override::class.java)
            .returns(InstanceRetention::class.java)
            .addStatement("return \$T.\$L", InstanceRetention::class.java, method.instanceRetention)
            .build()
    }

    override fun visitExit(factoryType: FactoryType) {

        val createMethod = MethodSpec
            .methodBuilder("create")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(
                ParameterSpec
                    .builder(Scope::class.java, PARAM_SCOPE_NAME)
                    .build()
            )
            .returns(factoryType.interfaceType)
            .addCode(createMethodCodeBuilder!!.build())
            .addStatement("return new \$T($constructorParametersBuilder)", factoryType.instanceType)
            .build()

        val getTypeMethod = MethodSpec
            .methodBuilder("getType")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(Class::class.java)
            .addStatement("return \$T.class", factoryType.interfaceType)
            .build()

        factoryTypeSpec = typeBuilder!!
            .addMethod(createMethod)
            .addMethod(getInstanceRetention)
            .addMethod(getTypeMethod)
            .build()
    }

    fun getTypeSpec(): TypeSpec {
        return factoryTypeSpec!!
    }

}
