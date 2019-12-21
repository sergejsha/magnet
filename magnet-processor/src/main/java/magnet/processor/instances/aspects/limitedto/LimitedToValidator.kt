package magnet.processor.instances.aspects.limitedto

import magnet.Scoping
import magnet.processor.common.throwValidationError
import magnet.processor.instances.parser.AspectValidator
import magnet.processor.instances.parser.ParserInstance
import javax.lang.model.element.Element

object LimitedToValidator : AspectValidator {
    override fun <E : Element> ParserInstance<E>.validate(): ParserInstance<E> {
        if (limitedTo == "*") {
            element.throwValidationError(
                "Limit must not use reserved '*' value. Use another value."
            )
        } else if (limitedTo.isNotEmpty() && scoping == Scoping.UNSCOPED.name) {
            element.throwValidationError(
                "Limit can only be used with Scoping.TOPMOST and Scoping.DIRECT." +
                    " Current scoping: Scoping.$scoping"
            )
        }
        return this
    }
}
