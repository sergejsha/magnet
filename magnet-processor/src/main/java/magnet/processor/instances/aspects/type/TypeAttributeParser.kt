package magnet.processor.instances.aspects.type

import magnet.processor.common.verifyInheritance
import magnet.processor.instances.parser.AttributeParser
import magnet.processor.instances.parser.ParserInstance
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element

object TypeAttributeParser : AttributeParser("type") {
    override fun <E : Element> Scope<E>.parse(value: AnnotationValue): ParserInstance<E> =
        env.elements.getTypeElement(value.value.toString())?.let {
            if (isTypeInheritanceEnforced) it.verifyInheritance(element, env.types)
            instance.copy(declaredType = it)
        } ?: instance
}
