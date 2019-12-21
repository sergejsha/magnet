package magnet.processor.instances.aspects.disabled

import magnet.processor.instances.parser.AttributeParser
import magnet.processor.instances.parser.ParserInstance
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element

object DisabledAttributeParser : AttributeParser("disabled") {
    override fun <E : Element> Scope<E>.parse(value: AnnotationValue): ParserInstance<E> =
        instance.copy(disabled = value.value.toString().toBoolean())
}
