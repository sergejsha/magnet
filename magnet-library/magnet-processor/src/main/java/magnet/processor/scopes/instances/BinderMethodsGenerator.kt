package magnet.processor.scopes.instances

import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import magnet.Classifier
import magnet.processor.scopes.AspectGenerator
import magnet.processor.scopes.Model
import javax.lang.model.element.Modifier

internal class BinderMethodsGenerator : AspectGenerator() {

    private val builders = mutableListOf<MethodSpec.Builder>()

    override fun enterScope(scope: Model.Scope) {
        builders.clear()
    }

    override fun visitBindMethod(method: Model.BindMethod) {
        var builder = MethodSpec
            .methodBuilder(method.name)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override::class.java)
            .addParameter(method.instance.type, method.instance.name)

        with(method.instance) {
            builder = if (classifier == Classifier.NONE) {
                builder
                    .addStatement(
                        "requireScopeContainer().bind(\$T.class, \$L, \$T.NONE)",
                        type, name, Classifier::class.java
                    )
            } else {
                builder
                    .addStatement(
                        "requireScopeContainer().bind(\$T.class, \$L, \$S)",
                        type, name, classifier
                    )
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