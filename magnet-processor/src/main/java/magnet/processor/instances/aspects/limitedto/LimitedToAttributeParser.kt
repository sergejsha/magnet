package magnet.processor.instances.aspects.limitedto

import magnet.processor.instances.parser.AttributeParser
import magnet.processor.instances.parser.ParserInstance
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element

object LimitedToAttributeParser : AttributeParser("limitedTo") {
    override fun <E : Element> Scope<E>.parse(value: AnnotationValue): ParserInstance<E> =
        instance.copy(limitedTo = value.value.toString())
}
