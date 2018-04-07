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

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to be used on implementation classes. Magnet will generate factories
 * for those classes and allow to query those implementations using
 * {@link ImplementationManager}.
 */
@Retention(SOURCE)
@Target({ ElementType.TYPE })
public @interface Implementation {

    /**
     * Defines the interface this implementation implements. Same type is used
     * when implementations are queried by means of {@link ImplementationManager}.
     *
     * @see ImplementationManager#getMany(Class, DependencyScope)
     * @see ImplementationManager#getSingle(Class, DependencyScope)
     * @see ImplementationManager#requireSingle(Class, DependencyScope)
     */
    Class<?> type();

    /**
     * Defines an optional string used for querying an implementation associated
     * with a certain target. For instance same interface {@code MenuItem} is
     * used in main and debug menus. Implementations can dedicate themselves to
     * one of those menus by declaring "main" or "debug" as the target. Main
     * and debug menus will correspondingly query their menu items.
     *
     * @see ImplementationManager#getMany(Class, String, DependencyScope)
     * @see ImplementationManager#getSingle(Class, String, DependencyScope)
     * @see ImplementationManager#requireSingle(Class, String, DependencyScope)
     */
    String forTarget() default ImplementationManager.DEFAULT_TARGET;

    //Class<?> type();
    //String classifier() default ImplementationManager.DEFAULT_TARGET;

}