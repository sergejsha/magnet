package magnet.processor.factory.generator

import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import magnet.Scoping
import magnet.processor.factory.GetScopingMethod
import javax.lang.model.element.Modifier

internal class GetScopingMethodGenerator : AspectGenerator {

    private var getScoping: MethodSpec? = null

    override fun generate(classBuilder: TypeSpec.Builder) {
        getScoping?.let { classBuilder.addMethod(it) }
    }

    override fun reset() {
        getScoping = null
    }

    fun visit(method: GetScopingMethod) {
        val defaultImplementationAvailable = method.scoping == Scoping.TOPMOST.name
        if (!defaultImplementationAvailable) {
            getScoping = MethodSpec
                .methodBuilder("getScoping")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override::class.java)
                .returns(Scoping::class.java)
                .addStatement("return \$T.\$L", Scoping::class.java, method.scoping)
                .build()
        }
    }

}