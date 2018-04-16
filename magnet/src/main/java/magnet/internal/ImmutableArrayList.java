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

package magnet.internal;

import java.util.AbstractList;

/** Subject to change. For internal use only. */
public class ImmutableArrayList<E> extends AbstractList<E> {

    private final E[] elements;

    public ImmutableArrayList(E[] elements) {
        this.elements = elements;
    }

    @Override public E get(int i) {
        if (i < 0 || i >= elements.length) {
            throw new IndexOutOfBoundsException(
                    String.format(
                            "Cannot find element with index %s, array length: %s", i, elements.length));
        }
        return elements[i];
    }

    @Override public int size() {
        return elements.length;
    }
}
