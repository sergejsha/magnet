package magnet.processor.scopes.instances

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeVariableName
import magnet.internal.InternalFactory
import magnet.processor.scopes.AspectGenerator
import magnet.processor.scopes.Model
import javax.lang.model.element.Modifier

class CreateSubscopeGenerator : AspectGenerator() {

    private var generateMethod: Boolean = false

    override fun visitScope(scope: Model.Scope) {
        generateMethod = false
    }

    override fun visitCreateSubscopeMethod(method: Model.CreateSubscopeMethod) {
        generateMethod = true
    }

    override fun generate(typeBuilder: TypeSpec.Builder) {
        if (generateMethod) {
            val typeVariable = TypeVariableName.get("T")
            typeBuilder
                .addMethod(
                    MethodSpec
                        .methodBuilder("createSubscope")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override::class.java)
                        .addTypeVariable(TypeVariableName.get("T"))
                        .returns(typeVariable)
                        .addParameter(
                            ParameterizedTypeName.get(ClassName.get(Class::class.java), typeVariable),
                            "scopeType"
                        )
                        .addStatement(
                            "return \$T.createScope(scopeType, $SCOPE_CONTAINER_FIELD_NAME.createSubscope())",
                            InternalFactory::class.java
                        )
                        .build()
                )
        }
    }

}