package magnet.processor.instances.aspects.classifier

import magnet.processor.instances.parser.AttributeParser
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element

object ClassifierAttributeParser : AttributeParser("classifier") {
    override fun Scope.parse(value: AnnotationValue, element: Element) =
        instance.copy(classifier = value.value.toString())
}
