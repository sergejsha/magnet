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

/**
 * Dependency scope is a set of components provided by an extensible class
 * to the implementations of its extension interface.
 *
 * <p>
 * For instance, some implementations of an extension interface might want to
 * access {@code Resources} dependency. If extensible component provides this
 * dependency within the dependency scope used for getting the implementations
 * through {@link ImplementationManager}, then implementations may request
 * {@code Resources} in their constructor. Magnet will make sure that requested
 * dependency gets injected into the constructor by taking it from the provided
 * dependency scope instance.
 *
 * <p>
 * It is possible to create sub-scopes of already existing dependency scope and
 * add new dependencies to the sub-scope. This is useful when you don't want
 * implementations to overwrite components in your original scope. Then you
 * create an empty sub-scope instance and provide it to the implementations.
 * When implementation requests a dependency from a sub-scope, Magnet will look
 * in the sub-scope first and, if a dependency has not been found, it will
 * continue to search in parent scope and so on, until a dependency is found
 * or root scope is reached.
 *
 * @see ImplementationManager#getMany(Class, DependencyScope)
 * @see ImplementationManager#getMany(Class, String, DependencyScope)
 */
public interface DependencyScope {

    /**
     * Returns dependency of given type or null if none was found.
     *
     * @param type the type of dependency to search for.
     * @param <T>  the type of dependency to search for.
     * @return dependency found or {@code null} if none was found.
     */
    <T> T get(Class<T> type);

    /**
     * Returns dependency of given type. If dependency has not been
     * found, then method throws {@link IllegalArgumentException}.
     *
     * @param type type of dependency to search for.
     * @param <T>  type of dependency to search for.
     * @return dependency found.
     */
    <T> T require(Class<T> type);

    /**
     * Registers a new dependency within this scope. If dependency
     * of given type already exists, then implementation will throw an
     * {@link IllegalStateException}. If you want to avoid issues with
     * overwriting dependencies, then create a new {@link #subscope()} and
     * add dependencies in there.
     *
     * @param type       type of dependency to be registered.
     * @param dependency dependency to be registered.
     * @param <T>        type of dependency to be registered.
     * @return this dependency scope for building chained calls.
     */
    <T> DependencyScope register(Class<T> type, T dependency);

    /**
     * Creates a new dependency scope using this scope as the parent.
     *
     * @return new child dependency scope.
     */
    DependencyScope subscope();

}