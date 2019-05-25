package magnet.processor.instances.disposer

import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import magnet.processor.instances.FactoryType
import javax.lang.model.element.Modifier

internal class IsDisposableMethodGenerator {

    private var methodBuilder: MethodSpec.Builder? = null

    fun visitFactoryClass(factoryType: FactoryType) {
        methodBuilder = if (factoryType.disposerMethodName == null) null
        else MethodSpec
            .methodBuilder("isDisposable")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .returns(TypeName.BOOLEAN)
            .addStatement("return true")
    }

    fun generate(typeBuilder: TypeSpec.Builder) {
        methodBuilder?.let { typeBuilder.addMethod(it.build()) }
    }
}