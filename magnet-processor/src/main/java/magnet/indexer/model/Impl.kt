package magnet.indexer.model

data class Impl(
        val type: String,
        val target: String,
        val factory: String
) {
    fun accept(visitor: ImplVisitor) {
        visitor.visit(this)
    }
}
