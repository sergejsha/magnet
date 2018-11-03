package magnet.processor.scopes.factories

import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import magnet.processor.scopes.AspectGenerator
import magnet.processor.scopes.Model
import javax.lang.model.element.Modifier

class GetTypeMethodGenerator : AspectGenerator() {

    private lateinit var builder: MethodSpec.Builder

    override fun visitScope(scope: Model.Scope) {
        builder = MethodSpec.methodBuilder("getType")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .returns(Class::class.java)
            .addStatement("return \$T.class", scope.type)
    }

    override fun generate(typeBuilder: TypeSpec.Builder) {
        typeBuilder.addMethod(builder.build())
    }

}