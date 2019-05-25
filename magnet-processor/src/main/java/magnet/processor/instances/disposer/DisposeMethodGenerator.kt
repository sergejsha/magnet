package magnet.processor.instances.disposer

import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import magnet.processor.instances.FactoryType
import javax.lang.model.element.Modifier

internal class DisposeMethodGenerator {

    private var methodBuilder: MethodSpec.Builder? = null

    fun visitFactoryClass(factoryType: FactoryType) {
        methodBuilder = if (factoryType.disposerMethodName == null) null
        else MethodSpec
            .methodBuilder("dispose")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .returns(TypeName.VOID)
            .addParameter(factoryType.interfaceType, "instance")
            .addStatement(
                "((\$T) instance).\$L()",
                factoryType.implementationType, factoryType.disposerMethodName
            )
    }

    fun generate(typeBuilder: TypeSpec.Builder) {
        methodBuilder?.let { typeBuilder.addMethod(it.build()) }
    }
}