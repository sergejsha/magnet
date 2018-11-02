package magnet.processor.scopes

import com.squareup.javapoet.TypeSpec
import magnet.processor.factory.CodeWriter

abstract class Generator(
    private val classGenerator: ClassGenerator
) {

    private val aspects = mutableListOf<AspectGenerator>()
    private val visitor = object : Model.Visitor {

        override fun enterScope(scope: Model.Scope) {
            classGenerator.enterScope(scope)
            for (aspect in aspects) aspect.enterScope(scope)
        }

        override fun visitBindParentScope(method: Model.BindMethod) {
            classGenerator.visitBindParentScope(method)
            for (aspect in aspects) aspect.visitBindParentScope(method)
        }

        override fun visitBindMethod(method: Model.BindMethod) {
            classGenerator.visitBindMethod(method)
            for (aspect in aspects) aspect.visitBindMethod(method)
        }

        override fun visitGetterMethod(method: Model.GetterMethod) {
            classGenerator.visitGetterMethod(method)
            for (aspect in aspects) aspect.visitGetterMethod(method)
        }

        override fun exitScope() {
            classGenerator.exitScope()
            for (aspect in aspects) aspect.exitScope()
        }
    }

    fun registerAspect(aspectGenerator: AspectGenerator) {
        aspects.add(aspectGenerator)
    }

    fun generate(scope: Model.Scope): CodeWriter {
        scope.accept(visitor)
        val typeBuilder = classGenerator.generate()
        for (aspect in aspects) aspect.generate(typeBuilder)
        val typeSpec = typeBuilder.build()
        val packageName = classGenerator.packageName
        return CodeWriter(packageName, typeSpec)
    }

}

abstract class ClassGenerator : Model.Visitor {
    abstract val packageName: String
    abstract fun generate(): TypeSpec.Builder
}

abstract class AspectGenerator : Model.Visitor {
    abstract fun generate(typeBuilder: TypeSpec.Builder)
}

