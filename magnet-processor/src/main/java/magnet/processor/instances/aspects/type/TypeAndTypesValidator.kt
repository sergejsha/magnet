package magnet.processor.instances.aspects.type

import com.squareup.javapoet.ClassName
import magnet.Instance
import magnet.Scoping
import magnet.processor.common.throwCompilationError
import magnet.processor.common.throwValidationError
import magnet.processor.instances.parser.AspectValidator
import magnet.processor.instances.parser.ParserInstance
import javax.lang.model.element.Element

object TypeAndTypesValidator : AspectValidator {
    override fun <E : Element> ParserInstance<E>.validate(): ParserInstance<E> {
        val isTypeDeclared = declaredType != null
        val areTypesDeclared = declaredTypes?.isNotEmpty() ?: false

        if (!isTypeDeclared && !areTypesDeclared)
            element.throwValidationError(
                "${Instance::class.java} must declare either 'type' or 'types' property."
            )

        if (isTypeDeclared && areTypesDeclared)
            element.throwValidationError(
                "${Instance::class.java} must declare either 'type' or 'types' property, not both."
            )

        if (declaredType != null) {
            val types = arrayListOf(declaredType)
            return copy(
                declaredTypes = arrayListOf(declaredType),
                types = types.map { ClassName.get(it) }
            )
        }

        if (declaredTypes != null) {
            if (scoping == Scoping.UNSCOPED.name)
                element.throwValidationError(
                    "types() property must be used with scoped instances only. Set " +
                        "scoping to Scoping.DIRECT or Scoping.TOPMOST."
                )
            return copy(
                types = declaredTypes.map { ClassName.get(it) }
            )
        }

        element.throwCompilationError("Cannot verify type declaration.")
    }
}
