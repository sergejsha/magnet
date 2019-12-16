package magnet.processor.instances.aspects.disposer

import magnet.Scoping
import magnet.processor.common.ValidationException
import magnet.processor.instances.parser.ParsedInstance
import magnet.processor.instances.parser.AnnotationValidator
import javax.lang.model.element.Element

internal class DisposerAnnotationValidator : AnnotationValidator {

    override fun validate(instance: ParsedInstance, element: Element) {
        if (instance.disposer != null && instance.scoping == Scoping.UNSCOPED.name) {
            throw ValidationException(
                element = element,
                message = "Disposer cannot be used with UNSCOPED instances."
            )
        }
    }
}
