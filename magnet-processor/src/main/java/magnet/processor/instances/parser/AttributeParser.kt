package magnet.processor.instances.parser

import magnet.processor.MagnetProcessorEnv
import magnet.processor.instances.aspects.classifier.ClassifierAttributeParser
import magnet.processor.instances.aspects.disabled.DisabledAttributeParser
import magnet.processor.instances.aspects.disposer.DisposerAttributeParser
import magnet.processor.instances.aspects.factory.FactoryAttributeParser
import magnet.processor.instances.aspects.limitedto.LimitedToAttributeParser
import magnet.processor.instances.aspects.scoping.ScopingAttributeParser
import magnet.processor.instances.aspects.selector.SelectorAttributeParser
import magnet.processor.instances.aspects.type.TypeAttributeParser
import magnet.processor.instances.aspects.type.TypesAttributeParser
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element

abstract class AttributeParser(val name: String) {

    data class Scope(
        val isTypeInheritanceEnforced: Boolean,
        val instance: ParserInstance,
        val element: Element,
        val env: MagnetProcessorEnv
    )

    abstract fun Scope.parse(value: AnnotationValue, element: Element): ParserInstance

    object Registry {
        val PARSERS = mutableMapOf(
            ClassifierAttributeParser.name to ClassifierAttributeParser,
            DisabledAttributeParser.name to DisabledAttributeParser,
            DisposerAttributeParser.name to DisposerAttributeParser,
            FactoryAttributeParser.name to FactoryAttributeParser,
            LimitedToAttributeParser.name to LimitedToAttributeParser,
            ScopingAttributeParser.name to ScopingAttributeParser,
            SelectorAttributeParser.name to SelectorAttributeParser,
            TypeAttributeParser.name to TypeAttributeParser,
            TypesAttributeParser.name to TypesAttributeParser
        )
    }
}
