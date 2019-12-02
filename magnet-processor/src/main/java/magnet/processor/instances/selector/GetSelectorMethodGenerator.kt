package magnet.processor.instances.selector

import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import magnet.processor.instances.AspectGenerator
import magnet.processor.instances.GetSelectorMethod
import javax.lang.model.element.Modifier

internal class GetSelectorMethodGenerator : AspectGenerator {

    private var methodSpec: MethodSpec? = null
    private var constantFieldSpec: FieldSpec? = null
    private var constantInitializer: CodeBlock.Builder? = null
    private var argumentsLeft: Int = 0

    override fun generate(classBuilder: TypeSpec.Builder) {
        constantFieldSpec?.let { classBuilder.addField(it) }
        methodSpec?.let { classBuilder.addMethod(it) }
    }

    override fun reset() {
        methodSpec = null
        constantFieldSpec = null
        constantInitializer = null
        argumentsLeft = 0
    }

    fun enterGetSelectorMethod(method: GetSelectorMethod) {
        methodSpec = MethodSpec
            .methodBuilder("getSelector")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override::class.java)
            .returns(ArrayTypeName.of(String::class.java))
            .addStatement("return SELECTOR")
            .build()
        constantInitializer = CodeBlock.builder().add("{ ")
        argumentsLeft = method.selectorArguments.size
    }

    fun visitSelectorArgument(argument: String) {
        if (--argumentsLeft > 0) {
            checkNotNull(constantInitializer).add("\$S, ", argument)
        } else {
            checkNotNull(constantInitializer).add("\$S }", argument)
        }
    }

    fun exitGetSelectorMethod() {
        constantFieldSpec = FieldSpec
            .builder(ArrayTypeName.of(String::class.java), "SELECTOR")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .initializer(checkNotNull(constantInitializer).build())
            .build()
    }
}
