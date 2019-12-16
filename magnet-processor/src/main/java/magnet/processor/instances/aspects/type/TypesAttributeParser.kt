package magnet.processor.instances.aspects.type

import magnet.processor.common.verifyInheritance
import magnet.processor.instances.parser.AttributeParser
import magnet.processor.instances.parser.ParserInstance
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.SimpleAnnotationValueVisitor6

object TypesAttributeParser : AttributeParser("types") {

    override fun Scope.parse(value: AnnotationValue, element: Element): ParserInstance {
        val typesExtractor = TypesExtractor(env.elements)
            .apply { value.accept(this, null) }

        val interfaceTypeElements = typesExtractor.getExtractedValue() ?: return instance
        if (isTypeInheritanceEnforced) {
            for (typeElement in interfaceTypeElements) {
                typeElement.verifyInheritance(element, env.types)
            }
        }
        return instance.copy(declaredTypes = interfaceTypeElements)
    }
}

private class TypesExtractor(private val elements: Elements) :
    SimpleAnnotationValueVisitor6<Void?, Void>() {

    private val extractedTypes = mutableListOf<String>()

    fun getExtractedValue(): List<TypeElement>? =
        if (extractedTypes.isEmpty()) null
        else extractedTypes.map { elements.getTypeElement(it) }

    override fun visitArray(values: MutableList<out AnnotationValue>?, p: Void?): Void? {
        values?.let { for (value in values) value.accept(this, p) }
        return p
    }

    override fun visitType(typeMirror: TypeMirror?, p: Void?): Void? {
        typeMirror?.let { extractedTypes.add(it.toString()) }
        return p
    }
}
