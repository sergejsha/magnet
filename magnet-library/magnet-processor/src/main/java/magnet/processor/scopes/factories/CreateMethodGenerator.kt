package magnet.processor.scopes.factories

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import magnet.processor.scopes.AspectGenerator
import magnet.processor.scopes.Model
import magnet.processor.scopes.getGeneratedScopeImplementationName
import javax.lang.model.element.Modifier

class CreateMethodGenerator : AspectGenerator() {

    private lateinit var builder: MethodSpec.Builder

    override fun enterScope(scope: Model.Scope) {
        val factoryClass = ClassName.get(scope.packageName, scope.getGeneratedScopeImplementationName())
        builder = MethodSpec.methodBuilder("create")
            .addModifiers(Modifier.PUBLIC)
            .returns(scope.type)
            .addAnnotation(Override::class.java)
            .addStatement("return new \$T()", factoryClass)
    }

    override fun generate(typeBuilder: TypeSpec.Builder) {
        typeBuilder.addMethod(builder.build())
    }

}