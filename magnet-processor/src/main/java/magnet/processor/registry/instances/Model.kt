package magnet.processor.registry.instances

import com.squareup.javapoet.ClassName

class Model private constructor() {

    class Index(
        val instances: List<Inst>,
        val sections: List<Section>
    ) {

        fun accept(visitor: IndexVisitor) {
            visitor.visit(this)
            sections.forEach {
                it.accept(visitor)
            }
        }

    }

    class Inst(
        val type: String,
        val classifier: String,
        val factory: ClassName
    ) {
        fun accept(visitor: InstVisitor) {
            visitor.visit(this)
        }
    }

    class InstComparator : Comparator<Inst> {
        override fun compare(left: Inst, right: Inst): Int {
            val c1 = left.type.compareTo(right.type)
            if (c1 != 0) {
                return c1
            }
            val c2 = left.classifier.compareTo(right.classifier)
            if (c2 != 0) {
                return c2
            }
            return left.factory.compareTo(right.factory)
        }
    }

    interface InstVisitor {
        fun visit(inst: Inst)
    }

    class Range(
        val type: String,
        val classifier: String,
        private val inst: Inst,
        val from: Int
    ) {
        val impls = mutableListOf<Inst>()
        val firstFactory
            get() = impls[0].factory

        init {
            impls.add(inst)
        }

        fun accept(visitor: IndexVisitor) {
            visitor.visit(this)
            impls.forEach {
                it.accept(visitor)
            }
        }
    }

    class Section(
        val type: String
    ) {
        val ranges = mutableMapOf<String, Range>()
        val firstFactory
            get() = ranges.values.elementAt(0).firstFactory

        fun accept(visitor: IndexVisitor) {
            visitor.visit(this)
            ranges.forEach {
                it.value.accept(visitor)
            }
        }
    }

    interface IndexVisitor : InstVisitor {
        fun visit(index: Index)
        fun visit(section: Section)
        fun visit(range: Range)
    }

}