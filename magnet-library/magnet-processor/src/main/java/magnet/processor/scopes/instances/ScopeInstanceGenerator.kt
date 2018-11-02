package magnet.processor.scopes.instances

import com.squareup.javapoet.TypeSpec
import magnet.processor.MagnetProcessorEnv
import magnet.processor.factory.CodeWriter
import magnet.processor.scopes.Model

class ScopeInstanceGenerator(env: MagnetProcessorEnv) {

    private val scopeInstanceVisitor = ScopeInstanceVisitor()

    fun generate(scope: Model.Scope): CodeWriter {
        scope.accept(scopeInstanceVisitor)
        val typeSpec = scopeInstanceVisitor.generate()
        val packageName = scopeInstanceVisitor.packageName
        return CodeWriter(packageName, typeSpec)
    }

}

/*

@Scope
interface AppScope {
    void bind(GlobalScope parent);
    void bind(Object obj);
    void bind(@Classifier("timer") Long obj);
    String getString();
    List<String> getStrings();
}

// generated
class MagnetAppScope extends InstanceScope implements AppScope {

    MagnetAppScope() {
        super(true);
    }

    @Override public void bind(GlobalScope parent) {
        bindParentScope((InstanceScope) parent);
    }

    @Override public void bind(Object object) {
        bindInstance(Object.class, object, Classifier.NONE);
    }

    @Override public void bind(Long obj) {
        bindInstance(Long.class, obj, "timer");
    }

    @Override public String getString() {
        return getSingle(String.class, Classifier.NONE);
    }

    @Override public List<String> getStrings() {
        return getMany(String.class, Classifier.NONE);
    }

}

 */

private class ScopeInstanceVisitor : Model.Visitor {


    private val classGenerator = ClassGenerator()
    private val constructorGenerator = ConstructorGenerator()
    private val getterMethodsGenerator = GetterMethodsGenerator()

    private lateinit var scope: Model.Scope

    val packageName: String get() = scope.packageName

    override fun enterScope(scope: Model.Scope) {
        this.scope = scope
        classGenerator.enterScope(scope)
        constructorGenerator.enterScope()
        getterMethodsGenerator.enterScope()
    }

    override fun visitBindParentScope(method: Model.BindMethod) {
        // todo
    }

    override fun visitBindMethod(method: Model.BindMethod) {
        // todo
    }

    override fun visitGetterMethod(method: Model.GetterMethod) {
        getterMethodsGenerator.visitGetterMethod(method)
    }

    override fun exitScope() {
        // todo
    }

    fun generate(): TypeSpec {
        val typeBuilder = classGenerator.generate()
        constructorGenerator.generate(typeBuilder)
        getterMethodsGenerator.generate(typeBuilder)
        return typeBuilder.build()
    }

}
