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

package magnet

/**
 * Returns dependency of given type or null if none was found.
 *
 * @receiver Scope
 * @param T the type of dependency to search for.
 * @return  dependency found or `null` if none was found.
 */
inline fun <reified T> Scope.get(classifier: String = Classifier.NONE): T? {
    return this.getOptional(T::class.java, classifier)
}

/**
 * Returns dependency of given type or fails with [IllegalStateException]
 * if none was found.
 *
 * @receiver Scope
 * @param T the type of dependency to search for.
 * @return  dependency found.
 */
inline fun <reified T> Scope.getSingle(classifier: String = Classifier.NONE): T {
    return this.getSingle(T::class.java, classifier)
}

inline fun <reified T> Scope.getMany(classifier: String = Classifier.NONE): List<T> {
    return this.getMany(T::class.java, classifier)
}

/**
 * Registers a new dependency within this scope. If dependency
 * of given type already exists, then implementation will throw an
 * [IllegalStateException]. If you want to avoid issues with
 * overwriting dependencies, then create a new subscope and
 * add dependencies in there.
 */
inline fun <reified T> Scope.register(component: T, classifier: String = Classifier.NONE) {
    this.register(T::class.java, component, classifier)
}