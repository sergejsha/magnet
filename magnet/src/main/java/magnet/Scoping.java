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
 * Declares the way Magnet binds instances of &#64;Implementation</code>-annotated
 * classes into scopes. See each separate setting for more detail.
 */
public enum Scoping {

    /**
     * Magnet will not bind created instance into any scope. This is equivalent
     * to having a factory creating new instances each time when instance is
     * requested.
     *
     * <p>
     * In the example below, both instances {@code typeA} and {@code typeB} are
     * <b>not</b> bound into the {@code scope}. Each new {@code scope.getSingle(TypeB.class)}
     * call will return new instances of {@code typeA} and {@code typeB}.
     *
     * <pre>
     *
     * &#64;Implementation(type = TypeA.class, scoping = Scoping.NONE)
     * class TypeA {
     *     TypeA() {}
     * }
     *
     * &#64;Implementation(type = TypeB.class, scoping = Scoping.NONE)
     * class TypeB {
     *     final TypeA typeA;
     *     TypeB(@NonNull TypeA dependency) {
     *         typeA = dependency;
     *     }
     * }
     *
     * ...
     *
     * Scope scope = Magnet.createScope();
     *
     * TypeB typeB = scope.getSingle(TypeB.class);
     * TypeA typeA = typeB.typeA;
     * TypeA typeA2 = scope.getSingle(TypeA.class);
     * typeA !== typeA2 // different instances
     *
     * </pre>
     */
    NONE,

    /**
     * Magnet will bind created instance into the most top scope in the chain of scopes,
     * where all dependencies for the created instance are still fulfilled.
     *
     * <p>
     * Scopes in Magnet can build a chain of parent-child relations (see {@link Scope} for
     * more detail). This option allows binding instances of annotated class into a one
     * of parent scopes. Magnet goes up the scope chain and checks, whether all dependencies
     * for the instance can still be satisfied in that scope. The most top reached scope
     * with satisfied dependencies is the scope, into which the instance gets bound.
     *
     * <p>
     * In the example below both instances {@code typeA} and {@code typeB} are bound into
     * {@code root}.
     *
     * <pre>
     *
     * &#64;Implementation(type = TypeA.class, scoping = Scoping.TOPMOST)
     * class TypeA {
     *     TypeA() {}
     * }
     *
     * &#64;Implementation(type = TypeB.class)
     * class TypeB {
     *     final TypeA typeA;
     *     TypeB(@NonNull TypeA dependency) {
     *         typeA = dependency;
     *     }
     * }
     *
     * ...
     *
     * Scope root = Magnet.createScope();
     * Scope scope = root.createSubscope();
     *
     * TypeB typeB = scope.getSingle(TypeB.class);
     * TypeA typeA = typeB.typeA;
     * TypeA typeA2 = scope.getSingle(TypeA.class);
     * typeA === typeA2 // same instance
     *
     * </pre>
     *
     * Explanation:
     *
     * <ul>
     * <li>{@code TypeA} has no dependencies and thus it gets bound to {@code root};
     * <li>{@code TypeB} depends on {@code TypeA} and because the instance of {@code TypeA}
     * is available in {@code root}, and instance of {@code TypeB} can also be created
     * in {@code root}.
     * </ul>
     *
     * <p>
     * In the example below we bind instance of {@code typeA} into {@code scope} and
     * {@code typeB} instance gets bound into the {@code scope} too.
     *
     * <pre>
     *
     * &#64;Implementation(type = TypeA.class, scoping = Scoping.TOPMOST)
     * class TypeA {
     *     TypeA() {}
     * }
     *
     * &#64;Implementation(type = TypeB.class, scoping = Scoping.TOPMOST)
     * class TypeB {
     *     final TypeA typeA;
     *     TypeB(@NonNull TypeA dependency) {
     *         typeA = dependency;
     *     }
     * }
     *
     * ...
     *
     * Scope root = Magnet.createScope();
     * Scope scope = root.createSubscope().bind(TypeA.class, new TypeA());
     *
     * TypeB typeB = scope.getSingle(TypeB.class);
     * TypeA typeA = typeB.typeA;
     * TypeA typeA2 = scope.getSingle(TypeA.class);
     * typeA === typeA2 // same instance
     *
     * </pre>
     *
     * Explanation:
     *
     * <ul>
     * <li>{@code typeB} cannot be bound in a scope above its dependencies;
     * <li>{@code typeA} is available in {@code scope}, thus the {@code scope} is the
     * top most scope for dependent {@code typeB} too.
     * </ul>
     */
    TOPMOST,

    /**
     * Magnet will bind created instance into the same scope, in which this instance
     * has been created.
     *
     * <p>
     * In the example below, both instances {@code typeA} and {@code typeB} are bound
     * into {@code scope}. Each new {@code scope.getSingle(TypeB.class)} call on the
     * same instance of {@code scope} will return same instances of {@code typeA} and
     * {@code typeB}.
     *
     * <pre>
     *
     * &#64;Implementation(type = TypeA.class, scoping = Scoping.DIRECT)
     * class TypeA {
     *     TypeA() {}
     * }
     *
     * &#64;Implementation(type = TypeB.class, scoping = Scoping.DIRECT)
     * class TypeB {
     *     final TypeA typeA;
     *     TypeB(@NonNull TypeA dependency) {
     *         typeA = dependency;
     *     }
     * }
     *
     * ...
     *
     * Scope root = Magnet.createScope();
     * Scope scope = root.createSubscope();
     *
     * TypeB typeB = scope.getSingle(TypeB.class);
     * TypeA typeA = typeB.typeA;
     * TypeA typeA2 = scope.getSingle(TypeA.class);
     * typeA === typeA2 // same instance
     *
     * </pre>
     */
    DIRECT
}
