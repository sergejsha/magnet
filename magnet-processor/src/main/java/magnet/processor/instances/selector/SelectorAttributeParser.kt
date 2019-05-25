package magnet.processor.instances.selector

import magnet.processor.common.ValidationException
import magnet.processor.instances.AttributeParser
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element

class SelectorAttributeParser : AttributeParser<String> {

    override val attrName: String = "selector"

    fun convert(selector: String, element: Element): List<String>? {
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

    override fun parse(value: AnnotationValue, element: Element): String {
        return value.value.toString()
    }

    companion object {
        private val DELIMITER = Regex("[?!\\s|.]+")
        private val OPERATORS = arrayListOf(">", "<", ">=", "<=", "==", "!=", "in", "!in")
    }
}