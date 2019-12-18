package magnet.processor.instances.aspects.factory

import com.squareup.javapoet.TypeName
import magnet.processor.instances.parser.AttributeParser
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element

object FactoryAttributeParser : AttributeParser("factory") {
    override fun Scope.parse(value: AnnotationValue, element: Element) =
        instance.copy(factory = parseFactoryType(value))

    private fun Scope.parseFactoryType(value: AnnotationValue): TypeName? {
        val factoryTypeElement = env.annotation.getTypeElement(value)
        return TypeName.get(factoryTypeElement.asType())
    }
}