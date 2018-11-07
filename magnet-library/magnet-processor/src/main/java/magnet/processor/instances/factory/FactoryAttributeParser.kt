package magnet.processor.instances.factory

import com.squareup.javapoet.TypeName
import magnet.processor.MagnetProcessorEnv
import magnet.processor.common.AnnotationValueExtractor
import magnet.processor.instances.AttributeParser
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element

class FactoryAttributeParser(
    env: MagnetProcessorEnv
) : AttributeParser<TypeName?> {

    private val annotationValueExtractor = AnnotationValueExtractor(env.elements)

    override fun parse(value: AnnotationValue, element: Element): TypeName? {
        val factoryTypeElement = annotationValueExtractor.getTypeElement(value)
        return TypeName.get(factoryTypeElement.asType())
    }

    companion object {
        const val ATTR_NAME = "factory"
    }

}