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

package magnet.processor.registry.instances

import magnet.processor.registry.instances.Model.Index
import magnet.processor.registry.instances.Model.Inst
import magnet.processor.registry.instances.Model.InstComparator

class Indexer(
    private val comparator: Comparator<Inst> = InstComparator()
) {

    fun index(instances: List<Inst>): Index {
        val sorted = instances.sortedWith(comparator)

        val indexer = SectionsCreatorVisitor()
        sorted.forEach {
            it.accept(indexer)
        }

        return Index(sorted, indexer.sections)
    }

}


