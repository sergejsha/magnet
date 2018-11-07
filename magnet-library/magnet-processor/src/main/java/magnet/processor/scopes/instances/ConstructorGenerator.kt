package magnet.processor.scopes.instances

import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import magnet.ScopeContainer
import magnet.processor.scopes.AspectGenerator
import javax.lang.model.element.Modifier

internal const val SCOPE_CONTAINER_FIELD_NAME = "scopeContainer"

class ConstructorGenerator : AspectGenerator() {

    override fun generate(typeBuilder: TypeSpec.Builder) {
        typeBuilder
            .addField(
                FieldSpec
                    .builder(ScopeContainer::class.java, SCOPE_CONTAINER_FIELD_NAME)
                    .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
                    .build()
            )
            .addMethod(
                MethodSpec
                    .constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ScopeContainer::class.java, SCOPE_CONTAINER_FIELD_NAME)
                    .addStatement("this.$SCOPE_CONTAINER_FIELD_NAME = $SCOPE_CONTAINER_FIELD_NAME")
                    .build()
            )
    }

}