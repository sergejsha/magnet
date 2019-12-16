package magnet.processor.instances.parser

import magnet.processor.MagnetProcessorEnv
import magnet.processor.instances.aspects.classifier.ClassifierAttributeParser
import magnet.processor.instances.aspects.disabled.DisabledAttributeParser
import magnet.processor.instances.aspects.disposer.DisposerAttributeParser
import magnet.processor.instances.aspects.factory.FactoryAttributeParser
import magnet.processor.instances.aspects.limitedto.LimitedToAttributeParser
import magnet.processor.instances.aspects.scoping.ScopingAttributeParser
import magnet.processor.instances.aspects.selector.SelectorAttributeParser
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element

abstract class AttributeParser(val name: String) {

    data class Scope(
        val instance: ParsedInstance,
        val element: Element,
        val env: MagnetProcessorEnv
    )

    abstract fun Scope.parse(value: AnnotationValue, element: Element): ParsedInstance

    fun parseInScope(scope: Scope, value: AnnotationValue, element: Element): ParsedInstance =
        scope.parse(value, element)

    object Registry {
        private val parsers: MutableMap<String, AttributeParser> = mutableMapOf()

        init {
            register(ClassifierAttributeParser)
            register(DisabledAttributeParser)
            register(DisposerAttributeParser)
            register(FactoryAttributeParser)
            register(LimitedToAttributeParser)
            register(ScopingAttributeParser)
            register(SelectorAttributeParser)
        }

        private fun register(parser: AttributeParser) {
            parsers[parser.name] = parser
        }

        fun getAttributeParser(name: String): AttributeParser? = parsers[name]
    }
}
