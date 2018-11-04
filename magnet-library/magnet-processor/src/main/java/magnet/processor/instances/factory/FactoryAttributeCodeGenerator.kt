package magnet.processor.instances.factory

import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import magnet.processor.instances.FactoryType
import javax.lang.model.element.Modifier

class FactoryAttributeCodeGenerator {

    fun visitFactoryClass(builder: TypeSpec.Builder, factoryType: FactoryType) {
        if (factoryType.customFactoryType != null) {
            builder.addField(
                FieldSpec
                    .builder(factoryType.customFactoryType, "factory")
                    .addModifiers(Modifier.PRIVATE)
                    .initializer("null")
                    .build()
            )
        }
    }

    fun visitCreateMethod(builder: MethodSpec.Builder, factoryType: FactoryType) {
        if (factoryType.customFactoryType != null) {
            builder.addCode(
                CodeBlock.builder()
                    .beginControlFlow("if (factory == null)")
                    .addStatement("factory = new \$T()", factoryType.customFactoryType)
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