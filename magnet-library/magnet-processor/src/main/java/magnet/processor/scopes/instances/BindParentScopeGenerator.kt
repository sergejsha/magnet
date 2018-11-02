package magnet.processor.scopes.instances

import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import magnet.internal.InstanceScope
import magnet.processor.scopes.Model
import javax.lang.model.element.Modifier

class BindParentScopeGenerator {

    private var builder: MethodSpec.Builder? = null

    fun enterScope() {
        builder = null
    }

    fun visitBindParentScope(method: Model.BindMethod) {
        builder = MethodSpec.methodBuilder(method.name)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override::class.java)
            .addParameter(method.instance.type, method.instance.name)
            .addStatement("setParentScope((\$T) \$L)", InstanceScope::class.java, method.instance.name)
    }

    fun generate(typeBuilder: TypeSpec.Builder) {
        builder?.let {
            typeBuilder.addMethod(it.build())
        }
    }

}