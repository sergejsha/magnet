package magnet.processor.scopes.instances

import com.squareup.javapoet.TypeSpec
import magnet.processor.factory.CodeWriter
import magnet.processor.scopes.Model

class ScopeInstanceGenerator {

    private val scopeInstanceVisitor = ScopeInstanceVisitor()

    fun generate(scope: Model.Scope): CodeWriter {
        scope.accept(scopeInstanceVisitor)
        val typeSpec = scopeInstanceVisitor.generate()
        val packageName = scopeInstanceVisitor.packageName
        return CodeWriter(packageName, typeSpec)
    }

}

private class ScopeInstanceVisitor : Model.Visitor {

    private val classGenerator = ClassGenerator()
    private val constructorGenerator = ConstructorGenerator()
    private val getterMethodsGenerator = GetterMethodsGenerator()
    private val binderMethodsGenerator = BinderMethodsGenerator()
    private val bindParentScopeGenerator = BindParentScopeGenerator()

    private lateinit var scope: Model.Scope

    val packageName: String get() = scope.packageName

    override fun enterScope(scope: Model.Scope) {
        this.scope = scope
        classGenerator.enterScope(scope)
        constructorGenerator.enterScope(scope)
        getterMethodsGenerator.enterScope()
        binderMethodsGenerator.enterScope()
        bindParentScopeGenerator.enterScope()
    }

    override fun visitBindParentScope(method: Model.BindMethod) {
        bindParentScopeGenerator.visitBindParentScope(method)
    }

    override fun visitBindMethod(method: Model.BindMethod) {
        binderMethodsGenerator.visitBindMethod(method)
    }

    override fun visitGetterMethod(method: Model.GetterMethod) {
        getterMethodsGenerator.visitGetterMethod(method)
    }

    override fun exitScope() {}

    fun generate(): TypeSpec {
        val typeBuilder = classGenerator.generate()
        constructorGenerator.generate(typeBuilder)
        getterMethodsGenerator.generate(typeBuilder)
        binderMethodsGenerator.generate(typeBuilder)
        bindParentScopeGenerator.generate(typeBuilder)
        return typeBuilder.build()
    }

}
