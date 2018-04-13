package magnet.processor.model

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import javax.lang.model.element.Element

const val PARAM_SCOPE_NAME = "scope"

interface FactoryTypeVisitor {
    fun visitEnter(factoryType: FactoryType)
    fun visitEnter(createMethod: CreateMethod)
    fun visit(parameter: MethodParameter)
    fun visitExit(createMethod: CreateMethod)
    fun visit(method: GetRetentionMethod)
    fun visitExit(factoryType: FactoryType)
}

data class FactoryType(
    val element: Element,
    val factoryType: ClassName,
    val interfaceType: ClassName,
    val instanceType: ClassName,
    val createMethod: CreateMethod,
    val getRetentionMethod: GetRetentionMethod
) {
    fun accept(visitor: FactoryTypeVisitor) {
        visitor.visitEnter(this)
        createMethod.accept(visitor)
        getRetentionMethod.accept(visitor)
        visitor.visitExit(this)
    }
}

data class CreateMethod(
    val methodParameter: List<MethodParameter>
) {
    fun accept(visitor: FactoryTypeVisitor) {
        visitor.visitEnter(this)
        methodParameter.forEach { parameterNode ->
            parameterNode.accept(visitor)
        }
        visitor.visitExit(this)
    }
}

data class MethodParameter(
    val name: String,
    val type: TypeName,
    val classifier: String,
    val method: GetterMethod
) {
    fun accept(visitor: FactoryTypeVisitor) {
        visitor.visit(this)
    }
}

data class GetRetentionMethod(
    val instanceRetention: String
) {
    fun accept(visitor: FactoryTypeVisitor) {
        visitor.visit(this)
    }
}

enum class GetterMethod(val code: String) {

    GET_SINGLE("getSingle"),
    GET_OPTIONAL("getOptional"),
    GET_MANY("getMany"),
    GET_SCOPE("");

}
