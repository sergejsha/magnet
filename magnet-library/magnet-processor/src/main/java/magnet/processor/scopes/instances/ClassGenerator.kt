package magnet.processor.scopes.instances

import com.squareup.javapoet.TypeSpec
import magnet.internal.InstanceScope
import magnet.processor.scopes.Model
import javax.lang.model.element.Modifier

internal class ClassGenerator(
) {
    private val className get() = "MagnetInstance${scope.name}"

    private lateinit var scope: Model.Scope
    private lateinit var classBuilder: TypeSpec.Builder

    fun enterScope(scope: Model.Scope) {
        this.scope = scope
        classBuilder = TypeSpec.classBuilder(className)
            .addModifiers(Modifier.FINAL)
            .superclass(InstanceScope::class.java)
            .addSuperinterface(scope.type)
    }

    fun generate(): TypeSpec.Builder {
        return classBuilder
    }

}