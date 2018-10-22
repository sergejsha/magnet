package magnet.processor.factory.generator

import com.squareup.javapoet.ArrayTypeName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import magnet.processor.factory.GetSiblingTypesMethod
import javax.lang.model.element.Modifier

internal class GetSiblingTypesMethodGenerator {

    private var getSiblingTypes: MethodSpec? = null
    private var constBuilder: FieldSpec? = null
    private var constInitializer: CodeBlock.Builder? = null
    private var typesLeft: Int = 0

    fun enterSiblingTypesMethod(method: GetSiblingTypesMethod) {
        getSiblingTypes = MethodSpec
            .methodBuilder("getSiblingTypes")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(Override::class.java)
            .returns(ArrayTypeName.of(Class::class.java))
            .addStatement("return SIBLING_TYPES")
            .build()
        constInitializer = CodeBlock.builder().add("{ ")
        typesLeft = method.siblingTypes.size
    }

    fun visitSiblingType(type: ClassName) {
        if (--typesLeft > 0) {
            checkNotNull(constInitializer).add("\$T.class, ", type)
        } else {
            checkNotNull(constInitializer).add("\$T.class }", type)
        }
    }

    fun exitSiblingTypesMethod() {
        constBuilder = FieldSpec
            .builder(ArrayTypeName.of(Class::class.java), "SIBLING_TYPES")
            .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
            .initializer(checkNotNull(constInitializer).build())
            .build()
    }

    fun reset() {
        getSiblingTypes = null
        typesLeft = 0
    }

    fun generate(classBuilder: TypeSpec.Builder) {
        constBuilder?.let { classBuilder.addField(it) }
        getSiblingTypes?.let { classBuilder.addMethod(it) }
    }

}