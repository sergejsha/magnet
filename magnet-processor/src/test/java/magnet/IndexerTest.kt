package magnet

import com.google.common.truth.Truth.assertThat
import magnet.indexer.Indexer
import magnet.indexer.model.Impl
import org.junit.Test

class IndexerTest {

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
                Impl("CType", "four", "Factory"),
                Impl("CType", "two", "Factory"),
                Impl("CType", "one", "Factory"),
                Impl("CType", "three", "Factory"),
                Impl("CType", "four", "Factory"),

                Impl("BType", "", "Factory"),
                Impl("BType", "", "Factory"),
                Impl("BType", "", "Factory"),

                Impl("AType", "", "Factory"),
                Impl("AType", "one", "Factory"),
                Impl("AType", "", "Factory"),
                Impl("AType", "two", "Factory"),
                Impl("AType", "one", "Factory")
        )
    }

    fun sortedNodes(): List<Impl> {
        return listOf(
                Impl("AType", "", "Factory"),
                Impl("AType", "", "Factory"),
                Impl("AType", "one", "Factory"),
                Impl("AType", "one", "Factory"),
                Impl("AType", "two", "Factory"),
                Impl("BType", "", "Factory"),
                Impl("BType", "", "Factory"),
                Impl("BType", "", "Factory"),
                Impl("CType", "four", "Factory"),
                Impl("CType", "four", "Factory"),
                Impl("CType", "one", "Factory"),
                Impl("CType", "three", "Factory"),
                Impl("CType", "two", "Factory")
        )
    }

}