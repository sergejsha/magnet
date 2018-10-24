package magnet.processor.index.selector

import com.squareup.javapoet.ClassName
import magnet.Magnetizer
import magnet.processor.MagnetProcessorEnv
import magnet.processor.eachAnnotation
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.SimpleAnnotationValueVisitor6

private const val ATTR_NAME = "selectorFilters"

class SelectorFiltersParser {

    fun extractSelectorFilters(
        element: Element?, env: MagnetProcessorEnv
    ): List<ClassName> {
        if (element == null) return emptyList()

        val selectorFilters = mutableListOf<ClassName>()
        val classNamesExtractor by lazy { ClassNamesExtractor(env.elements) }
        element.eachAnnotation<Magnetizer> { name, value ->
            if (name == ATTR_NAME) {
                classNamesExtractor.extract(value, selectorFilters)
            }
        }

        return selectorFilters
    }

}

internal class ClassNamesExtractor(
    private val elements: Elements
) : SimpleAnnotationValueVisitor6<Void?, Void>() {

    private val collectedTypes = mutableListOf<String>()

    fun extract(value: AnnotationValue, selectorFilters: MutableList<ClassName>) {
        collectedTypes.clear()
        value.accept(this, null)
        for (type in collectedTypes) {
            selectorFilters.add(ClassName.get(elements.getTypeElement(type)))
        }
    }

    override fun visitArray(values: MutableList<out AnnotationValue>?, p: Void?): Void? {
        values?.let { for (value in values) value.accept(this, p) }
        return p
    }

    override fun visitType(typeMirror: TypeMirror?, p: Void?): Void? {
        typeMirror?.let { collectedTypes.add(it.toString()) }
        return p
    }
}
