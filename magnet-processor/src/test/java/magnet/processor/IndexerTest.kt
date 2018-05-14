package magnet.processor

import com.google.common.truth.Truth.assertThat
import magnet.processor.index.Indexer
import magnet.processor.index.model.Inst
import org.junit.Test

class IndexerTest {

    @Test
    fun test_IndexNodes() {
        val index = Indexer().index(unsortedNodes())

        assertThat(index.instances.toTypedArray()
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

    private fun unsortedNodes(): List<Inst> {
        return listOf(
            Inst("CType", "four", "Factory"),
            Inst("CType", "two", "Factory"),
            Inst("CType", "one", "Factory"),
            Inst("CType", "three", "Factory"),
            Inst("CType", "four", "Factory"),

            Inst("BType", "", "Factory"),
            Inst("BType", "", "Factory"),
            Inst("BType", "", "Factory"),

            Inst("AType", "", "Factory"),
            Inst("AType", "one", "Factory"),
            Inst("AType", "", "Factory"),
            Inst("AType", "two", "Factory"),
            Inst("AType", "one", "Factory")
        )
    }

    private fun sortedNodes(): List<Inst> {
        return listOf(
            Inst("AType", "", "Factory"),
            Inst("AType", "", "Factory"),
            Inst("AType", "one", "Factory"),
            Inst("AType", "one", "Factory"),
            Inst("AType", "two", "Factory"),
            Inst("BType", "", "Factory"),
            Inst("BType", "", "Factory"),
            Inst("BType", "", "Factory"),
            Inst("CType", "four", "Factory"),
            Inst("CType", "four", "Factory"),
            Inst("CType", "one", "Factory"),
            Inst("CType", "three", "Factory"),
            Inst("CType", "two", "Factory")
        )
    }

}