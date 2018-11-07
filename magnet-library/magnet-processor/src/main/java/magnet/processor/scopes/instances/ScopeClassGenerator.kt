package magnet.processor.scopes.instances

import com.squareup.javapoet.TypeSpec
import magnet.processor.scopes.ClassGenerator
import magnet.processor.scopes.Model
import magnet.processor.scopes.getGeneratedScopeImplementationName
import javax.lang.model.element.Modifier

internal class ScopeClassGenerator : ClassGenerator() {

    private lateinit var scope: Model.Scope
    private lateinit var classBuilder: TypeSpec.Builder

    override val packageName: String by lazy { scope.packageName }

    override fun visitScope(scope: Model.Scope) {
        this.scope = scope
        classBuilder = TypeSpec
            .classBuilder(scope.getGeneratedScopeImplementationName())
            .addModifiers(Modifier.FINAL)
            .addSuperinterface(scope.type)
    }

    override fun generate(): TypeSpec.Builder {
        return classBuilder
    }

}