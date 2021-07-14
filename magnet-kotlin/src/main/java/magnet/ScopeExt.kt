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

/** Returns an object from the scope or `null`, if object was not found. */
inline fun <reified T> Scope.getOptional(classifier: String = Classifier.NONE): T? =
    this.getOptional(T::class.java, classifier)

/** Returns an object from the scope or throws exception, if object was not found. */
inline fun <reified T> Scope.getSingle(classifier: String = Classifier.NONE): T =
    this.getSingle(T::class.java, classifier)

/** Returns a list of objects or empty list, if no objects were found. */
inline fun <reified T> Scope.getMany(classifier: String = Classifier.NONE): List<T> =
    this.getMany(T::class.java, classifier)

/** Bind given instance into this scope. */
inline fun <reified T : Any> Scope.bind(instance: T, classifier: String = Classifier.NONE) =
    this.bind(T::class.java, instance, classifier)

/** Creates a subscope of the current scope. */
inline fun Scope.createSubscope(init: Scope.() -> Unit): Scope =
    this.createSubscope().apply(init)
