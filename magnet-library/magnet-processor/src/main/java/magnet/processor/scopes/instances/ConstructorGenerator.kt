package magnet.processor.scopes.instances

import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import magnet.processor.scopes.Model
import javax.lang.model.element.Modifier

class ConstructorGenerator {

    private lateinit var builder: MethodSpec.Builder

    fun enterScope(scope: Model.Scope) {
        val hasParentScope = if (scope.bindParentScopeMethod == null) "false" else "true"
        builder = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addStatement("super(\$L)", hasParentScope)
    }

    fun generate(typeBuilder: TypeSpec.Builder) {
        typeBuilder.addMethod(builder.build())
    }

}