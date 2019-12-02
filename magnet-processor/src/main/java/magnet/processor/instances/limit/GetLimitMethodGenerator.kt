package magnet.processor.instances.limit

import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import magnet.processor.instances.AspectGenerator
import magnet.processor.instances.GetLimitMethod
import javax.lang.model.element.Modifier

internal class GetLimitMethodGenerator : AspectGenerator {

    private var getLimit: MethodSpec? = null

    override fun reset() {
        getLimit = null
    }

    override fun generate(classBuilder: TypeSpec.Builder) {
        getLimit?.let { classBuilder.addMethod(it) }
    }

    fun visit(method: GetLimitMethod) {
        getLimit = MethodSpec
            .methodBuilder("getLimit")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override::class.java)
            .returns(String::class.java)
            .addStatement("return \$S", method.limit)
            .build()
    }
}
