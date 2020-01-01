package magnet.processor.instances.parser

import magnet.processor.MagnetProcessorEnv
import magnet.processor.instances.aspects.disposer.DisposerValidator
import magnet.processor.instances.aspects.limitedto.LimitedToValidator
import magnet.processor.instances.aspects.type.TypeAndTypesValidator
import javax.lang.model.element.Element

interface AspectValidator {
    fun <E : Element> ParserInstance<E>.validate(env: MagnetProcessorEnv): ParserInstance<E>

    object Registry {
        val VALIDATORS: List<AspectValidator> = listOf(
            TypeAndTypesValidator,
            DisposerValidator,
            LimitedToValidator
        )
    }
}
