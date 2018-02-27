package magnet.indexer.model

data class Index(
        val implementations: List<Impl>,
        val sections: List<Section>
) {

    fun accept(visitor: IndexVisitor) {
        visitor.visit(this)
        sections.forEach {
            it.accept(visitor)
        }
    }

}
