package magnet.processor.instances.aspects.disabled

import magnet.processor.instances.parser.AttributeParser
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element

object DisabledAttributeParser : AttributeParser("disabled") {
    override fun Scope.parse(value: AnnotationValue, element: Element) =
        instance.copy(disabled = value.value.toString().toBoolean())
}
