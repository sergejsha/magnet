package magnet.processor.factory.selector

import magnet.processor.MagnetProcessorEnv
import magnet.processor.factory.AspectParser
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.Element

class SelectorAttributeParser(
    private val env: MagnetProcessorEnv
) : AspectParser<String> {

    fun convert(selector: String, element: Element): List<String>? {
        if (selector.isEmpty()) return null

        val parsedSelector = selector.split(DELIMITER)
        var isSelectorInvalid: Boolean = parsedSelector.size < 4
            || parsedSelector[0] != "android"
            || parsedSelector[1] != "api"
            || parsedSelector[2] !in OPERATORS
            || parsedSelector[3].toIntOrNull() == null

        if (!isSelectorInvalid) {
            isSelectorInvalid = when (parsedSelector[2]) {
                "in", "!in" -> parsedSelector.size != 5
                else -> parsedSelector.size != 4
            }
        }

        if (isSelectorInvalid) {
            throw env.compilationError(element, "Invalid selector. Expected format: 'android.api" +
                " [comparison operator] [api level]'. Supported comparison operators: $OPERATORS." +
                " Example selectors: 'android.api >= 28', 'android.api in 0..24'")
        }

        return parsedSelector
    }

    override fun parse(value: AnnotationValue, element: Element): String {
        return value.value.toString()
    }

    companion object {
        const val ATTR_NAME = "selector"

        private val DELIMITER = Regex("[?!\\s|.]+")
        private val OPERATORS = arrayListOf(">", "<", ">=", "<=", "==", "!=", "in", "!in")
    }

}