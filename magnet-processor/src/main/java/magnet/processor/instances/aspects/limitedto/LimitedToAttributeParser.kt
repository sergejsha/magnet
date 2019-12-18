package magnet.processor.instances.aspects.limitedto

import magnet.processor.instances.parser.AttributeParser
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element

object LimitedToAttributeParser : AttributeParser("limitedTo") {
    override fun Scope.parse(value: AnnotationValue, element: Element) =
        instance.copy(limitedTo = value.value.toString())
}
