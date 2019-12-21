package magnet.processor.instances.aspects.classifier

import magnet.processor.instances.parser.AttributeParser
import magnet.processor.instances.parser.ParserInstance
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element

object ClassifierAttributeParser : AttributeParser("classifier") {
    override fun <E : Element> Scope<E>.parse(value: AnnotationValue): ParserInstance<E> =
        instance.copy(classifier = value.value.toString())
}
