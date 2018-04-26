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

inline fun <reified T> Scope.getOptional(classifier: String = Classifier.NONE): T? {
    return this.getOptional(T::class.java, classifier)
}

inline fun <reified T> Scope.getSingle(classifier: String = Classifier.NONE): T {
    return this.getSingle(T::class.java, classifier)
}

inline fun <reified T> Scope.getMany(classifier: String = Classifier.NONE): List<T> {
    return this.getMany(T::class.java, classifier)
}

inline fun <reified T> Scope.bind(component: T, classifier: String = Classifier.NONE) {
    this.bind(T::class.java, component, classifier)
}

/** Create a sub scope in actual scope. */
inline fun Scope.createSubscope(init: Scope.() -> Unit): Scope {
    return this.createSubscope().apply(init)
}
