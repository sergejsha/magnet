package magnet.indexer

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import magnet.indexer.model.Impl
import magnet.indexer.model.Index
import magnet.indexer.model.IndexVisitor
import magnet.indexer.model.Range
import magnet.indexer.model.Section

class IndexGeneratorVisitor : IndexVisitor {

    val indexBuilder = CodeBlock.builder()
    val targetsBuilder = CodeBlock.builder()

    private var generateSingleRange = false
    private var currentSection: Section? = null
    private var sectionIndex = 0

    override fun visit(impl: Impl) {
        // nop
    }

    override fun visit(index: Index) {
        // nop
    }

    override fun visit(section: Section) {

        generateSingleRange = section.ranges.size == 1
        currentSection = section

        if (generateSingleRange) {
            return;
        }

        val targetsName = "ranges${++sectionIndex}"

        indexBuilder.addStatement(
                "index.put(\$T.class, \$L)",
                ClassName.bestGuess(section.type),
                targetsName
        )

        targetsBuilder.addStatement(
                "\$T<\$T, \$T> \$L = new \$T<>()",
                Map::class.java,
                String::class.java,
                magnet.internal.Range::class.java,
                targetsName,
                HashMap::class.java
        )
    }

    override fun visit(range: Range) {

        if (generateSingleRange) {
            currentSection?.let {
                indexBuilder.addStatement(
                        "index.put(\$T.class, new \$T(\$L, \$L, \$S))",
                        ClassName.bestGuess(it.type),
                        magnet.internal.Range::class.java,
                        range.from,
                        range.impls.size,
                        range.target
                )
            }
            return
        }

        val targetsName = "ranges${sectionIndex}"

        targetsBuilder.addStatement(
                "\$L.put(\$S, new \$T(\$L, \$L, \$S))",
                targetsName,
                range.target,
                magnet.internal.Range::class.java,
                range.from,
                range.impls.size,
                range.target
        )
    }

}