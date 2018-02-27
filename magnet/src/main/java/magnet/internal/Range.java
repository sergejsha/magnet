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

/** For internal use only. */
public final class Range {

    private final int from;
    private final int count;
    private final String target;

    public Range(int from, int count, String target) {
        this.from = from;
        this.count = count;
        this.target = target;
    }

    public int getFrom() {
        return from;
    }

    public int getCount() {
        return count;
    }

    public String getTarget() {
        return target;
    }
}
