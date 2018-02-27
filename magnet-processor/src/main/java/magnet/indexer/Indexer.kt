package magnet.indexer

import magnet.indexer.model.Impl
import magnet.indexer.model.ImplComparator
import magnet.indexer.model.Index

class Indexer(
        private val comparator: Comparator<Impl> = ImplComparator()
) {

    fun index(impls: List<Impl>): Index {
        val sorted = impls.sortedWith(comparator)

        val indexer = SectionsCreatorVisitor()
        sorted.forEach {
            it.accept(indexer)
        }

        return Index(sorted, indexer.sections)
    }

}


