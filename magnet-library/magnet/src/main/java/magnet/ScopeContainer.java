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

package magnet;

import java.util.List;

/** Runtime container for instances. */
public interface ScopeContainer {

    /** Returns an object from the scope or {@code null}, if object was not found. */
    <T> T getOptional(Class<T> type);

    /** Returns an object from the scope or {@code null}, if object was not found. */
    <T> T getOptional(Class<T> type, String classifier);

    /** Returns an object from the scope or throws exception, if object was not found. */
    <T> T getSingle(Class<T> type);

    /** Returns an object from the scope or throws exception, if object was not found. */
    <T> T getSingle(Class<T> type, String classifier);

    /** Returns a list of objects or empty list, if no objects were found. */
    <T> List<T> getMany(Class<T> type);

    /** Returns a list of objects or empty list, if no objects were found. */
    <T> List<T> getMany(Class<T> type, String classifier);

    /** Bind given instance into this scope. */
    <T> ScopeContainer bind(Class<T> type, T instance);

    /** Bind given instance into this scope. */
    <T> ScopeContainer bind(Class<T> type, T instance, String classifier);

    /** Creates a new child scope of this scope. */
    ScopeContainer createSubscope();

}