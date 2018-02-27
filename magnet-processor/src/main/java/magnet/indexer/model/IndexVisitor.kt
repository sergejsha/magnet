package magnet.indexer.model

interface IndexVisitor : ImplVisitor {
    fun visit(index: Index)
    fun visit(section: Section)
    fun visit(range: Range)
}