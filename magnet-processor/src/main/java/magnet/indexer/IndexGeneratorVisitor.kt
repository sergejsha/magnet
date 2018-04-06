/*
 * Copyright (C) 2018 Sergej Shafarenka, www.halfbit.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
            "index.put(\$T.getType(), \$L)",
            ClassName.bestGuess(section.firstFactory),
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
                    "index.put(\$T.getType(), new \$T(\$L, \$L, \$S))",
                    ClassName.bestGuess(range.firstFactory),
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