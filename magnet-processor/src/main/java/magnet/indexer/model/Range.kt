package magnet.indexer.model

data class Range(
        val type: String,
        val target: String,
        private val impl: Impl,
        val from: Int
) {
    val impls = mutableListOf<Impl>()

    init {
        impls.add(impl)
    }

    fun accept(visitor: IndexVisitor) {
        visitor.visit(this)
        impls.forEach {
            it.accept(visitor)
        }
    }
}
