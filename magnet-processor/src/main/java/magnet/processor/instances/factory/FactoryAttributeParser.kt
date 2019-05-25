package magnet.processor.instances.factory

import com.squareup.javapoet.TypeName
import magnet.processor.common.AnnotationValueExtractor
import magnet.processor.instances.AttributeParser
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element

class FactoryAttributeParser(
    private val extractor: AnnotationValueExtractor
) : AttributeParser<TypeName?> {

    override val attrName: String = "factory"

    override fun parse(value: AnnotationValue, element: Element): TypeName? {
        val factoryTypeElement = extractor.getTypeElement(value)
        return TypeName.get(factoryTypeElement.asType())
    }
}