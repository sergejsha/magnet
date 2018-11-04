package magnet.processor.instances.factory

import com.squareup.javapoet.ClassName
import magnet.processor.MagnetProcessorEnv
import magnet.processor.common.AnnotationValueExtractor
import magnet.processor.instances.AttributeParser
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element

class FactoryAttributeParser(
    private val env: MagnetProcessorEnv
) : AttributeParser<ClassName?> {

    private val annotationValueExtractor = AnnotationValueExtractor(env.elements)

    override fun parse(value: AnnotationValue, element: Element): ClassName? {
        val factoryTypeElement = annotationValueExtractor.getTypeElement(value)
        return ClassName.get(factoryTypeElement)
    }

    companion object {
        const val ATTR_NAME = "factory"
    }

}