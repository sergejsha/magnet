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

package magnet.indexer.model

data class Range(
    val type: String,
    val classifier: String,
    private val impl: Impl,
    val from: Int
) {
    val impls = mutableListOf<Impl>()
    val firstFactory
        get() = impls[0].factory

    init {
        impls.add(impl)
    }

    fun accept(visitor: IndexVisitor) {
        visitor.visit(this)
        impls.forEach {
            it.accept(visitor)
        }
    }
}
