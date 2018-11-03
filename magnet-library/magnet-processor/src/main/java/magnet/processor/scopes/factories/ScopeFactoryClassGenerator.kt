package magnet.processor.scopes.factories

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import magnet.internal.ScopeFactory
import magnet.processor.scopes.ClassGenerator
import magnet.processor.scopes.Model
import magnet.processor.scopes.getGeneratedScopeFactoryName
import javax.lang.model.element.Modifier

class ScopeFactoryClassGenerator : ClassGenerator() {

    private lateinit var scope: Model.Scope
    private lateinit var classBuilder: TypeSpec.Builder

    override val packageName: String by lazy { scope.packageName }

    override fun visitScope(scope: Model.Scope) {
        this.scope = scope

        classBuilder = TypeSpec.classBuilder(scope.getGeneratedScopeFactoryName())
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .superclass(
                ParameterizedTypeName.get(
                    ClassName.get(ScopeFactory::class.java),
                    scope.type
                )
            )
    }

    override fun generate(): TypeSpec.Builder {
        return classBuilder
    }

}