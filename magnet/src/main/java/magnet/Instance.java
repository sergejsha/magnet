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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Magnet instantiates classes marked with this annotation automatically. Annotated
 * classes must have a single, none private constructor. Constructor is allowed to have
 * parameters which are dependencies for this instance. Magnet checks constructor
 * parameters and tries to resolve corresponding dependencies by looking into accessible
 * scopes. If no suitable instances were found in scopes, Magnet searches for
 * {@code Instance}-annotated classes and tried to instantiate them. If constructor's
 * dependencies cannot be fulfilled, Magnet will fail at runtime with corresponding
 * error message.
 *
 * <p>
 * In the example below we declare two dependent types and instantiate them in scope.
 *
 * <pre>
 *     &#64;Instance(type=TypeA.class)
 *     class TypeA {
 *         TypeA() {}
 *     }
 *
 *     &#64;Instance(type=TypeB.class)
 *     class TypeB {
 *         final TypeA typeA;
 *         TypeB(@NonNull TypeA dependency) {
 *             typeA = dependency;
 *         }
 *     }
 *
 *     ...
 *
 *     // get instance of typeB
 *     TypeB typeB = scope.getSingle(TypeB.class);
 *
 *     // typeA has been provided by Magnet
 *     typeB.typeA != null
 *
 * </pre>
 *
 * <p>
 * <b>Nullability.</b> Magnet is capable of detecting whether dependency can or cannot be null.
 * If a constructor's parameter is annotated as nullable and Magnet cannot provide instance of
 * parameter's type, then <code>null</code> is provided instead.
 *
 * <p>
 * <b>Classification.</b> Same interface can be implemented by many classes. Classifier is used
 * to differentiate between those implementations. See {@link Classifier} for more detail.
 *
 * <p>
 * <b>Scoping.</b> Magnet can bind created instances into scope for reuse. Instance can
 * specify whether and how its instances should be bound into the scope. See {@link Scoping}
 * for more detail.
 */
@Retention(CLASS)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Instance {

    /**
     * Type to use when annotated instance gets registered in scope. Annotated class must
     * actually implement this type.
     */
    Class<?> type() default void.class;

    /**
     * Multiple types to use when annotated instance gets registered in scope. Annotated
     * class must actually implement all these types. Properties {@link #type()} and
     * {@code #types()} are exclusively mutual and cannot be used together.
     */
    Class<?>[] types() default void.class;

    /**
     * Classifier to use when annotated instance gets registered in scope.
     */
    String classifier() default Classifier.NONE;

    /**
     * Scoping rule to be applied when instance of annotated class gets created.
     */
    Scoping scoping() default Scoping.TOPMOST;

    /**
     * Limit value to be used with {@link Scoping#TOPMOST} algorithm. If a limit value is
     * preset then the instance will be placed at or below the scope having same limit
     * value applied to it. If multiple scopes have same limit value, then the closest scope
     * to the scope where the instance gets requested is used.
     */
    String limit() default "";

    /**
     * <b>Experimental.</b> Magnet will only create instance of the annotated class if
     * this selector expression is true after evaluation. Magnet currently support single
     * type of expression:
     * <p>
     * <code>android.api (comparison operator) (api version)</code>
     * <p>
     * For instance, the expression <code>android.api >= 28</code> will only create
     * annotated instance if Build.VERSION.SDK_INT >= 28. For the other versions
     * <code>null</code> is returned. Make sure to use optional injection to handle
     * this case.
     */
    String selector() default SelectorFilter.DEFAULT_SELECTOR;

    /** Custom factory to be used for creating instance instead of the generated one. */
    Class<? extends Factory> factory() default Factory.class;

    /** Name of optional disposer method to be called, when whole scope gets disposed. */
    String disposer() default "";

    /** Magnet ignores this annotation when this flag is set to <code>true</code>. */
    boolean disabled() default false;

}
