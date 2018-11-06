package magnet.processor.scopes.instances

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import magnet.Classifier
import magnet.processor.common.CommonModel
import magnet.processor.scopes.AspectGenerator
import magnet.processor.scopes.Model
import javax.lang.model.element.Modifier

internal class GetterMethodsGenerator : AspectGenerator() {

    private val builders = mutableListOf<MethodSpec.Builder>()

    override fun visitScope(scope: Model.Scope) {
        builders.clear()
    }

    override fun visitGetterMethod(method: Model.GetterMethod) {
        var builder = MethodSpec
            .methodBuilder(method.name)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override::class.java)

        when (method.instance.cardinality) {
            CommonModel.Cardinality.Many -> {
                builder = builder
                    .returns(
                        ParameterizedTypeName.get(
                            ClassName.get(List::class.java),
                            method.instance.type
                        )
                    )
                    .addReturnInstanceStatement("getMany", method.instance)
            }
            CommonModel.Cardinality.Optional -> {
                builder = builder
                    .returns(method.instance.type)
                    .addReturnInstanceStatement("getOptional", method.instance)
            }
            CommonModel.Cardinality.Single -> {
                builder = builder
                    .returns(method.instance.type)
                    .addReturnInstanceStatement("getSingle", method.instance)
            }
        }

        builders.add(builder)
    }

    override fun generate(typeBuilder: TypeSpec.Builder) {
        for (builder in builders) {
            typeBuilder.addMethod(builder.build())
        }
    }

}

private fun MethodSpec.Builder.addReturnInstanceStatement(
    methodName: String, instance: CommonModel.Instance
): MethodSpec.Builder =
    if (instance.classifier == Classifier.NONE) {
        addStatement(
            "return $SCOPE_CONTAINER_FIELD_NAME.\$L(\$T.class, \$T.NONE)",
            methodName, instance.type, Classifier::class.java
        )
    } else {
        addStatement(
            "return $SCOPE_CONTAINER_FIELD_NAME.\$L(\$T.class, \$S)",
            methodName, instance.type, instance.classifier
        )
    }
