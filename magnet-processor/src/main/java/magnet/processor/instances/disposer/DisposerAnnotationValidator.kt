package magnet.processor.instances.disposer

import magnet.Scoping
import magnet.processor.common.ValidationException
import magnet.processor.instances.Annotation
import magnet.processor.instances.AnnotationValidator
import javax.lang.model.element.Element

internal class DisposerAnnotationValidator : AnnotationValidator {

    override fun validate(annotation: Annotation, element: Element) {
        if (annotation.disposer != null && annotation.scoping == Scoping.UNSCOPED.name) {
            throw ValidationException(
                element = element,
                message = "Disposer cannot be used with UNSCOPED instances."
            )
        }
    }
}