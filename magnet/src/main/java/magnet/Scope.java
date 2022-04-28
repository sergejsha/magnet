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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Scope is a container for objects which are stored there at runtime.
 *
 * <p>
 * <b>Binding.</b> Binding is the way of putting objects into the scope.
 *
 * <pre>
 * Scope root = Magnet.createScope()
 *                 .bind(app, Application.class)
 *                 .bind(String.class, "#FF0000", "red-color");
 * </pre>
 *
 * <p>
 * <b>Chaining.</b> Scopes can be chained using
 * parent-child relation, or rather a child-parent relation because
 * a child scope (aka subscope) holds a reference to its parent and not
 * the other way around.
 *
 * <pre>
 * Scope subscope = root.createSubscope()
 *                   .bind(activity, Context.class)
 *                   .bind(String.class, "#F30303", "red-color")
 *                   .bind(String.class, "#00FF00", "green-color");
 * </pre>
 *
 * <b>Provisioning.</b> Scope has multiple get-methods for providing
 * objects it stores. Magnet will look through the whole scope's chain up
 * to the root parent scope to provide an object. First found match gets
 * returned.
 *
 * <pre>
 * // object overwriting
 * String red = root.getSingle(String.class, "red-color"); // "#FF0000" (from root)
 * String red = subscope.getSingle(String.class, "red-color"); // "#F30303" (from subscope)
 *
 * // scope chaining
 * Application app = subscope.getSingle(Application.class); // app (from root)
 * Context context = subscope.getSingle(Context.class); // activity (from subscope)
 * String green = subscope.getSingle(String.class, "green-color"); // "#00FF00" (from subscope)
 *
 * // optional provisioning
 * String red = root.getOptional(String.class, "red-color"); "#FF0000" (from root)
 * String yellow = subscope.getSingle(String.class, "yellow-color"); // throws IllegalStateException
 * String yellow = subscope.getOptional(String.class, "yellow-color"); // null, optional was not found
 * </pre>
 *
 * <p>
 * <b>Automatic binding (injection).</b>
 * Magnet can instantiate {@link Instance}-annotated classes and bind their instances
 * into respective scopes automatically. If instantiated classes have dependencies, Magnet
 * will resolve those dependencies too. In this respect Magnet works as dependency injection
 * library.
 *
 * <p>In the example below Magnet will create instance of {@code toaster} by taking required
 * dependencies from the scopes.
 *
 * <pre>
 * &#64;Instance(type = Toaster.class)
 * class Toaster {
 *     Toaster(
 *         Application app,
 *         &#64;Classifier("red-color") String red,
 *         &#64;Classifier("green-color") String green
 *     ) { ... }
 *
 * Toaster toaster = subscope.getSingle(Toaster.class);
 * toaster != null
 * </pre>
 */
public interface Scope {

    /** Returns an object from the scope or {@code null}, if object was not found. */
    @Nullable <T> T getOptional(@NotNull Class<T> type);

    /** Returns an object from the scope or {@code null}, if object was not found. */
    @Nullable <T> T getOptional(@NotNull Class<T> type, @NotNull String classifier);

    /** Returns an object from the scope or throws exception, if object was not found. */
    @NotNull <T> T getSingle(@NotNull Class<T> type);

    /** Returns an object from the scope or throws exception, if object was not found. */
    @NotNull <T> T getSingle(@NotNull Class<T> type, @NotNull String classifier);

    /** Returns a list of objects or empty list, if no objects were found. */
    @NotNull <T> List<T> getMany(@NotNull Class<T> type);

    /** Returns a list of objects or empty list, if no objects were found. */
    @NotNull <T> List<T> getMany(@NotNull Class<T> type, @NotNull String classifier);

    /** Binds given instance into this scope. */
    @NotNull <T> Scope bind(@NotNull Class<T> type, @NotNull T instance);

    /** Binds given instance into this scope. */
    @NotNull <T> Scope bind(@NotNull Class<T> type, @NotNull T instance, @NotNull String classifier);

    /** Sets given limits to this scope. */
    @NotNull Scope limit(String... limits);

    /** Creates a new child scope of this scope. */
    @NotNull Scope createSubscope();

    /** Disposes this and all children scopes. Notifies instances with {@link magnet.Instance#disposer()}. */
    void dispose();

    /** Returns `true` is the scope is disposed, or `false` otherwise. */
    boolean isDisposed();

    /** Visits all instances and child scopes of given depth (use {@code Integer.MAX_VALUE} for visiting all scopes). */
    void accept(Visitor visitor, int depth);

}