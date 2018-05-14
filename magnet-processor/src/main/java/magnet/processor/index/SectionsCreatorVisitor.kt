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

package magnet.processor.index

import magnet.processor.index.model.Inst
import magnet.processor.index.model.InstVisitor
import magnet.processor.index.model.Range
import magnet.processor.index.model.Section

class SectionsCreatorVisitor : InstVisitor {

    val sections: List<Section>
        get() {
            return sectionsByType.map {
                it.value
            }
        }

    private val sectionsByType = mutableMapOf<String, Section>()
    private var currentRange: Range? = null

    override fun visit(inst: Inst) {

        if (currentRange == null) {
            addRange(inst)
            return
        }

        currentRange?.let {
            if (it.type == inst.type
                && it.classifier == inst.classifier) {
                it.impls.add(inst)
                return
            }
            addRange(inst)
            return
        }
    }

    private fun addRange(inst: Inst) {

        val section = sectionsByType.getOrPut(inst.type) {
            Section(inst.type)
        }

        val rangeIndex = calculateIndex()
        val range = Range(inst.type, inst.classifier, inst, rangeIndex)
        section.ranges[range.classifier] = range

        currentRange = range
    }

    private fun calculateIndex(): Int {
        currentRange?.let {
            return it.from + it.impls.size
        }
        return 0
    }

}
