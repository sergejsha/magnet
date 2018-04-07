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
 * Searches for all registered implementations of given type and given target
 * and creates instances of implementations found by providing them with given
 * dependencies.
 *
 * @receiver ImplementationManager
 * @param T type of implementations to search for. This is the same type as
 *      the one used in `Implementation.type()`.
 * @param classifier type of implementations to search for. This is the same
 *      type as the one used in `Implementation.type()`.
 * @param dependencyScope instance of dependency scope used by Magnet to
 *      inject dependencies into implementations, when they are instantiated.
 */
inline fun <reified T> ImplementationManager.getMany(
    classifier: String? = ImplementationManager.DEFAULT_TARGET,
    dependencyScope: DependencyScope
): List<T> {
    return this.getMany(T::class.java, classifier, dependencyScope)
}

/**
 * Searches for zero or one registered implementation of given type and given
 * target and creates an instance of the implementation found by providing
 * them with given dependencies.
 *
 * @receiver ImplementationManager
 * @param T type of the implementation to search for. This is the same type as
 *      the one used in `Implementation.type()`.
 * @param classifier type of the implementation to search for. This is the same
 *      type as the one used in `Implementation.type()`.
 * @param dependencyScope instance of dependency scope used by Magnet to inject
 *      dependencies into implementations, when they are instantiated.
 */
inline fun <reified T> ImplementationManager.getSingle(
    classifier: String? = ImplementationManager.DEFAULT_TARGET,
    dependencyScope: DependencyScope
): T? {
    return this.getSingle(T::class.java, classifier, dependencyScope)
}

/**
 * Searches for exactly one registered implementation of given type and given
 * target and creates an instance of the implementation found by providing
 * them with given dependencies.
 *
 * @receiver ImplementationManager
 * @param T type of the implementation to search for. This is the same type
 *      as the one used in `Implementation.type()`.
 * @param classifier type of the implementation to search for. This is the
 *      same type as the one used in `Implementation.type()`.
 * @param dependencyScope instance of dependency scope used by Magnet to
 *      inject dependencies into implementations, when they are instantiated.
 * @throws IllegalStateException if implementation could not be found.
 */
inline fun <reified T> ImplementationManager.requireSingle(
    classifier: String? = ImplementationManager.DEFAULT_TARGET,
    dependencyScope: DependencyScope
): T {
    return this.requireSingle(T::class.java, classifier, dependencyScope)
}