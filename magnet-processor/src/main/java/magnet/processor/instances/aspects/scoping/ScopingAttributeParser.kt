package magnet.processor.instances.aspects.scoping

import magnet.processor.instances.parser.AttributeParser
import magnet.processor.instances.parser.ParserInstance
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element

object ScopingAttributeParser : AttributeParser("scoping") {
    override fun <E : Element> Scope<E>.parse(value: AnnotationValue): ParserInstance<E> =
        instance.copy(scoping = value.value.toString())
}
