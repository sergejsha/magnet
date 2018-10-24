package magnet.processor.index.selector

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import magnet.SelectorFilter

class SelectorMapGenerator {

    val filtersVariableName = "filters"

    fun generateCodeBlock(selectorFilterClassNames: List<ClassName>): CodeBlock {
        val code = CodeBlock.builder()

        if (selectorFilterClassNames.isEmpty()) {
            code.addStatement("\$T<String, \$T> \$L = null",
                Map::class.java, SelectorFilter::class.java, filtersVariableName)
            return code.build()
        }

        /*
            Map<String, SelectorFilter> filters = new HashMap<>();
            AndroidSelectorFilter filter1 = new AndroidSelectorFilter()
            filters.put(filter1.getId(), filter1)
            ...
         */

        code.addStatement("\$T<String, \$T> \$L = new \$T<>(${selectorFilterClassNames.size})",
            Map::class.java, SelectorFilter::class.java, filtersVariableName, HashMap::class.java)

        for (index in selectorFilterClassNames.indices) {
            val filterName = "filter$index"
            val filterClass = selectorFilterClassNames[index]
            code.addStatement("\$T $filterName = new \$T()", SelectorFilter::class.java, filterClass)
            code.addStatement("\$L.put($filterName.getId(), $filterName)", filtersVariableName)
        }
        return code.build()
    }


}