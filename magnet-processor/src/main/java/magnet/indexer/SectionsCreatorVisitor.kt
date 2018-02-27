package magnet.indexer

import magnet.indexer.model.Impl
import magnet.indexer.model.ImplVisitor
import magnet.indexer.model.Range
import magnet.indexer.model.Section

class SectionsCreatorVisitor : ImplVisitor {

    val sections: List<Section>
        get() {
            return sectionsByType.map {
                it.value
            }
        }

    private val sectionsByType = mutableMapOf<String, Section>()
    private var currentRange: Range? = null

    override fun visit(impl: Impl) {

        if (currentRange == null) {
            addRange(impl)
            return
        }

        currentRange?.let {
            if (it.type == impl.type
                    && it.target == impl.target) {
                it.impls.add(impl)
                return
            }
            addRange(impl)
            return
        }
    }

    private fun addRange(impl: Impl) {

        val section = sectionsByType.getOrPut(impl.type) {
            Section(impl.type)
        }

        val rangeIndex = calculateIndex()
        val range = Range(impl.type, impl.target, impl, rangeIndex)
        section.ranges[range.target] = range

        currentRange = range
    }

    private fun calculateIndex(): Int {
        currentRange?.let {
            return it.from + it.impls.size
        }
        return 0
    }

}
