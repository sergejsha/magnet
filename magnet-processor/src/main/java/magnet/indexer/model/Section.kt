package magnet.indexer.model

data class Section(
        val type: String
) {
    val ranges = mutableMapOf<String, Range>()

    fun accept(visitor: IndexVisitor) {
        visitor.visit(this)
        ranges.forEach {
            it.value.accept(visitor)
        }
    }
}
