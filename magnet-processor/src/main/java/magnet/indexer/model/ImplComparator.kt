package magnet.indexer.model

class ImplComparator() : Comparator<Impl> {
    override fun compare(left: Impl, right: Impl): Int {
        val c1 = left.type.compareTo(right.type)
        if (c1 != 0) {
            return c1
        }
        val c2 = left.target.compareTo(right.target)
        if (c2 != 0) {
            return c2
        }
        return left.factory.compareTo(right.factory)
    }
}