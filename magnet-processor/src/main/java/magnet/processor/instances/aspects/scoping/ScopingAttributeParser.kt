package magnet.processor.instances.aspects.scoping

import magnet.processor.instances.parser.AttributeParser
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element

object ScopingAttributeParser : AttributeParser("scoping") {
    override fun Scope.parse(value: AnnotationValue, element: Element) =
        instance.copy(scoping = value.value.toString())
}
