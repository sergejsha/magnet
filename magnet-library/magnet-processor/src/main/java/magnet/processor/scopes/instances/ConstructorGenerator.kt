package magnet.processor.scopes.instances

import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier

class ConstructorGenerator {

    private lateinit var builder: MethodSpec.Builder

    fun enterScope() {
        builder = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addStatement("super(false)")
    }

    fun generate(typeBuilder: TypeSpec.Builder) {
        typeBuilder.addMethod(builder.build())
    }

}