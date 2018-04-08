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

class ImplComparator() : Comparator<Impl> {
    override fun compare(left: Impl, right: Impl): Int {
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