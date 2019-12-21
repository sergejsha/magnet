package magnet.processor.instances.aspects.disposer

import magnet.Scoping
import magnet.processor.common.throwValidationError
import magnet.processor.instances.parser.AspectValidator
import magnet.processor.instances.parser.ParserInstance
import javax.lang.model.element.Element

object DisposerValidator : AspectValidator {
    override fun <E : Element> ParserInstance<E>.validate(): ParserInstance<E> {
        if (disposer != null && scoping == Scoping.UNSCOPED.name)
            element.throwValidationError("Disposer cannot be used with UNSCOPED instances.")
        return this
    }
}
