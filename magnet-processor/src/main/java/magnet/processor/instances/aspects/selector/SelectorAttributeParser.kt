package magnet.processor.instances.aspects.selector

import magnet.processor.common.ValidationException
import magnet.processor.instances.parser.AttributeParser
import magnet.processor.instances.parser.ParserInstance
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element

object SelectorAttributeParser : AttributeParser("selector") {

    override fun <E : Element> Scope<E>.parse(value: AnnotationValue): ParserInstance<E> =
        instance.copy(selector = parse(value.value.toString()))

    private fun <E : Element> Scope<E>.parse(selector: String): List<String>? {
        if (selector.isEmpty()) return null

        val parsedSelector = selector.split(DELIMITER)
        var isSelectorInvalid: Boolean = parsedSelector.size < 4 ||
            parsedSelector[0].isEmpty() ||
            parsedSelector[1].isEmpty() ||
            parsedSelector[2] !in OPERATORS ||
            parsedSelector[3].isEmpty()

        if (!isSelectorInvalid) {
            isSelectorInvalid = when (parsedSelector[2]) {
                "in", "!in" -> parsedSelector.size != 5
                else -> parsedSelector.size != 4
            }
        }

        if (isSelectorInvalid) {
            throw ValidationException(
                element = element,
                message = "Invalid selector. Expected format:" +
                    " '[selector id].[selector field] [comparison operator] [value]'." +
                    " Supported comparison operators: $OPERATORS." +
                    " Example selectors: 'android.api >= 28', 'android.api in 0..24'"
            )
        }

        return parsedSelector
    }
}

private val DELIMITER = Regex("[?!\\s|.]+")
private val OPERATORS = arrayListOf(">", "<", ">=", "<=", "==", "!=", "in", "!in")
