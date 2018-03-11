package magnet

import com.google.common.truth.Truth.assertThat
import com.sun.tools.javac.model.JavacElements
import com.sun.tools.javac.util.Context
import magnet.indexer.Indexer
import magnet.indexer.model.Impl
import org.junit.Before
import org.junit.Test
import javax.lang.model.element.Element

class IndexerTest {

    private lateinit var factoryElement: Element

    @Before
    fun setUp() {
        val context = Context()
        val elements = JavacElements.instance(context)
        factoryElement = elements.getTypeElement(
                Object::class.java.canonicalName
        )
    }

    @Test
    fun test_IndexNodes() {
        val index = Indexer().index(unsortedNodes())

        assertThat(index.implementations.toTypedArray()
                contentDeepEquals sortedNodes().toTypedArray())
    }

    @Test
    fun test_IndexSections() {
        val index = Indexer().index(unsortedNodes())

        assertThat(index.sections.size).isEqualTo(3)

        assertThat(index.sections[0].type).isEqualTo("AType")
        assertThat(index.sections[0].ranges.size).isEqualTo(3)

        assertThat(index.sections[1].type).isEqualTo("BType")
        assertThat(index.sections[1].ranges.size).isEqualTo(1)

        assertThat(index.sections[2].type).isEqualTo("CType")
        assertThat(index.sections[2].ranges.size).isEqualTo(4)
    }

    fun unsortedNodes(): List<Impl> {
        return listOf(
                Impl("CType", "four", "Factory", factoryElement),
                Impl("CType", "two", "Factory", factoryElement),
                Impl("CType", "one", "Factory", factoryElement),
                Impl("CType", "three", "Factory", factoryElement),
                Impl("CType", "four", "Factory", factoryElement),

                Impl("BType", "", "Factory", factoryElement),
                Impl("BType", "", "Factory", factoryElement),
                Impl("BType", "", "Factory", factoryElement),

                Impl("AType", "", "Factory", factoryElement),
                Impl("AType", "one", "Factory", factoryElement),
                Impl("AType", "", "Factory", factoryElement),
                Impl("AType", "two", "Factory", factoryElement),
                Impl("AType", "one", "Factory", factoryElement)
        )
    }

    fun sortedNodes(): List<Impl> {
        return listOf(
                Impl("AType", "", "Factory", factoryElement),
                Impl("AType", "", "Factory", factoryElement),
                Impl("AType", "one", "Factory", factoryElement),
                Impl("AType", "one", "Factory", factoryElement),
                Impl("AType", "two", "Factory", factoryElement),
                Impl("BType", "", "Factory", factoryElement),
                Impl("BType", "", "Factory", factoryElement),
                Impl("BType", "", "Factory", factoryElement),
                Impl("CType", "four", "Factory", factoryElement),
                Impl("CType", "four", "Factory", factoryElement),
                Impl("CType", "one", "Factory", factoryElement),
                Impl("CType", "three", "Factory", factoryElement),
                Impl("CType", "two", "Factory", factoryElement)
        )
    }

}