package magnet.processor.instances.aspects.disposer

import magnet.processor.common.throwValidationError
import magnet.processor.instances.parser.AttributeParser
import magnet.processor.instances.parser.ParserInstance
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.type.TypeKind

object DisposerAttributeParser : AttributeParser("disposer") {
    override fun <E : Element> Scope<E>.parse(value: AnnotationValue): ParserInstance<E> =
        instance.copy(disposer = parseMethodName(value))

    private fun <E : Element> Scope<E>.parseMethodName(value: AnnotationValue): String {

        if (element.kind != ElementKind.CLASS)
            element.throwValidationError("Disposer can be defined for annotated class only.")

        val methodName = env.annotation
            .getStringValue(value)

            .removeSurrounding("\"")

        val methodElement = element.enclosedElements
            .find { it.kind == ElementKind.METHOD && it.simpleName.toString() == methodName }
            ?: element.throwValidationError("Instance must declare disposer method $methodName().")

        val returnType = (methodElement as ExecutableElement).returnType
        if (returnType.kind != TypeKind.VOID)
            element.throwValidationError("Disposer method $methodName() must return void.")

        if (methodElement.parameters.size != 0)
            element.throwValidationError("Disposer method $methodName() must have no parameters.")

        if (methodElement.modifiers.contains(Modifier.PRIVATE))
            element.throwValidationError("Disposer method $methodName() must not be 'private'.")

        return methodName
    }
}
