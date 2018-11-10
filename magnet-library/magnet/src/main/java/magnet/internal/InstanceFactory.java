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

import magnet.Scope;
import magnet.Scoping;

/* Subject to change. For internal use only. */
public abstract class InstanceFactory<T> {

    public abstract T create(Scope scope);
    public Scoping getScoping() { return Scoping.TOPMOST; }
    public Class[] getSiblingTypes() { return null; }
    public String[] getSelector() { return null; }

    public boolean isDisposable() { return false; }
    public void dispose(T instance) {
        throw new IllegalStateException(
            String.format("Instance %s is not disposable", instance)
        );
    }

}
