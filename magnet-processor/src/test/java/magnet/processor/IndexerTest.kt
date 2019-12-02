package magnet.processor

import com.google.common.truth.Truth.assertThat
import com.squareup.javapoet.ClassName
import magnet.processor.registry.instances.Indexer
import magnet.processor.registry.instances.Model.Inst
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
        val factory = ClassName.bestGuess("Factory")
        return listOf(
            Inst("CType", "four", factory),
            Inst("CType", "two", factory),
            Inst("CType", "one", factory),
            Inst("CType", "three", factory),
            Inst("CType", "four", factory),

            Inst("BType", "", factory),
            Inst("BType", "", factory),
            Inst("BType", "", factory),

            Inst("AType", "", factory),
            Inst("AType", "one", factory),
            Inst("AType", "", factory),
            Inst("AType", "two", factory),
            Inst("AType", "one", factory)
        )
    }

    private fun sortedNodes(): List<Inst> {
        val factory = ClassName.bestGuess("Factory")
        return listOf(
            Inst("AType", "", factory),
            Inst("AType", "", factory),
            Inst("AType", "one", factory),
            Inst("AType", "one", factory),
            Inst("AType", "two", factory),
            Inst("BType", "", factory),
            Inst("BType", "", factory),
            Inst("BType", "", factory),
            Inst("CType", "four", factory),
            Inst("CType", "four", factory),
            Inst("CType", "one", factory),
            Inst("CType", "three", factory),
            Inst("CType", "two", factory)
        )
    }
}
