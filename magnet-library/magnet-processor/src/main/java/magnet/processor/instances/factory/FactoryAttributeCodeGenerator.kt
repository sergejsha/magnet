package magnet.processor.instances.factory

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import magnet.processor.instances.FactoryType
import javax.lang.model.element.Modifier

class FactoryAttributeCodeGenerator {

    private lateinit var factoryFieldType: TypeName

    fun visitFactoryClass(builder: TypeSpec.Builder, factoryType: FactoryType) {
        factoryType.customFactoryType?.let {
            builder
                .addField(
                    FieldSpec
                        .builder(factoryFieldType, "factory")
                        .addModifiers(Modifier.PRIVATE)
                        .initializer("null")
                        .build()
                )
        }
    }

    fun visitCreateMethod(builder: MethodSpec.Builder, factoryType: FactoryType) {
        factoryType.customFactoryType?.let { customFactoryType ->
            factoryFieldType = when (customFactoryType) {
                is ParameterizedTypeName -> ParameterizedTypeName.get(customFactoryType.rawType, factoryType.type)
                else -> customFactoryType
            }

            builder.addCode(
                CodeBlock.builder()
                    .beginControlFlow("if (factory == null)")
                    .addStatement("factory = new \$T()", factoryFieldType)
                    .endControlFlow()
                    .addStatement(
                        "return factory.create(scope, \$T.class, \$S)",
                        factoryType.type, factoryType.classifier
                    )
                    .build()
            )
        }
    }

}