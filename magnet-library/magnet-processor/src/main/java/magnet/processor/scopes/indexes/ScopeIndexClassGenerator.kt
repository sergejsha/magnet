package magnet.processor.scopes.indexes

import com.squareup.javapoet.AnnotationSpec
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeSpec
import magnet.internal.Index
import magnet.internal.ScopeFactory
import magnet.processor.scopes.ClassGenerator
import magnet.processor.scopes.Model
import magnet.processor.scopes.getGeneratedScopeFactoryName
import javax.lang.model.element.Modifier

internal class ScopeIndexClassGenerator : ClassGenerator() {

    private lateinit var scope: Model.Scope
    private lateinit var classBuilder: TypeSpec.Builder

    override val packageName: String get() = "magnet.index"

    override fun enterScope(scope: Model.Scope) {
        this.scope = scope

        val className = ClassName.get(packageName, scope.getNameIncludingPackage())
        val factoryClassName = ClassName.get(scope.packageName, scope.getGeneratedScopeFactoryName())

        classBuilder = TypeSpec.classBuilder(className)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addAnnotation(
                AnnotationSpec.builder(Index::class.java)
                    .addMember("factoryType", "\$T.class", ScopeFactory::class.java)
                    .addMember("factoryClass", "\$T.class", factoryClassName)
                    .addMember("instanceType", "\$S", scope.type)
                    .addMember("classifier", "\$S", "")
                    .build()
            )
    }

    override fun generate(): TypeSpec.Builder {
        return classBuilder
    }

}

private fun Model.Scope.getNameIncludingPackage(): String =
    packageName.replace(".", "_") + "_${getGeneratedScopeFactoryName()}"
