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

/**
 * Manager for querying implementations registered in the application.
 *
 * <p>
 * Typically, an extensible component provides an interface, which has to be
 * implemented in order to extend functionality of this component. For example
 * a menu can provide a menu interface to be implemented by certain menu items.
 * Each implementation of the menu item interface will have to declare itself
 * as an {@link Implementation} of that interface in order to be discovered by
 * the menu. Menu implementation will use this manager to instantiate all
 * menu item implementations registered in the system.
 *
 * <p>
 * If property annotated implementation cannot be found, then make sure your
 * project has the class annotated with {@link MagnetizeImplementations}.
 *
 * @see Implementation
 * @see MagnetizeImplementations
 */
public interface ImplementationManager {

    /**
     * Searches system for all registered {@link Implementation}'s of given type
     * and creates instances of those.
     *
     * @param forType         type of implementations to searche for. This is the
     *                        same type used in {@link Implementation#forType()}.
     * @param dependencyScope instance of dependency scope used by Magnet to inject
     *                        dependencies into implementations, when they are
     *                        instantiated.
     * @param <T>             type of implementations to search for.
     * @return list of instantiated implementations or an empty list.
     */
    <T> List<T> get(Class<T> forType, DependencyScope dependencyScope);

    /**
     * Searches system for all registered {@link Implementation}'s of given type
     * plus target and creates instances of those.
     *
     * @param forType         type of implementations to search for. This is the same
     *                        type as the one used in {@link Implementation#forType()}.
     * @param forTarget       target of implementations to search for. This is the same
     *                        target as the one used in {@link Implementation#forTarget()}.
     * @param dependencyScope instance of dependency scope used by Magnet to inject
     *                        dependencies into implementations, when they are
     *                        instantiated.
     * @param <T>             type of implementations to search for.
     * @return list of instantiated implementations or an empty list.
     */
    <T> List<T> get(Class<T> forType, String forTarget, DependencyScope dependencyScope);

}