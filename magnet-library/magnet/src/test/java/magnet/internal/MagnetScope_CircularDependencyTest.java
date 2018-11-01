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

package magnet.internal;

import magnet.Scoping;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(JUnit4.class)
public class MagnetScope_CircularDependencyTest {

    private MagnetInstanceScope scope;

    @Before
    public void before() {
        scope = new MagnetInstanceScope(null, new StubInstanceManager());
    }

    @Test(expected = IllegalStateException.class)
    public void dependency_One_Two_Three_One() {
        scope.getSingle(MenuItem.class, "one");
    }

    @Test(expected = IllegalStateException.class)
    public void dependency_Four_Four() {
        scope.getSingle(MenuItem.class, "four");
    }

    @Test(expected = IllegalStateException.class)
    public void dependency_Five_Constructor_Five() {
        scope.getSingle(MenuItem.class, "five");
    }

    private static class MenuItemOneFactory extends InstanceFactory<MenuItem> {
        @Override public MenuItem create(InstanceScope scope) {
            scope.getSingle(MenuItem.class, "two");
            return new MenuItemOne();
        }
        @Override public Scoping getScoping() { return Scoping.TOPMOST; }
    }

    private static class MenuItemTwoFactory extends InstanceFactory<MenuItem> {
        @Override public MenuItem create(InstanceScope scope) {
            scope.getSingle(MenuItem.class, "three");
            return new MenuItemTwo();
        }
        @Override public Scoping getScoping() { return Scoping.TOPMOST; }
    }

    private static class MenuItemThreeFactory extends InstanceFactory<MenuItem> {
        @Override public MenuItem create(InstanceScope scope) {
            scope.getSingle(MenuItem.class, "one");
            return new MenuItemThree();
        }
        @Override public Scoping getScoping() { return Scoping.TOPMOST; }
    }

    private static class MenuItemFourFactory extends InstanceFactory<MenuItem> {
        @Override public MenuItem create(InstanceScope scope) {
            scope.getSingle(MenuItem.class, "four");
            return new MenuItemFour();
        }
        @Override public Scoping getScoping() { return Scoping.TOPMOST; }
    }

    private static class MenuItemFiveFactory extends InstanceFactory<MenuItem> {
        @Override public MenuItem create(InstanceScope scope) {
            return new MenuItemFive(scope);
        }
        @Override public Scoping getScoping() { return Scoping.TOPMOST; }
    }

    private static class StubInstanceManager implements InstanceManager {
        private final Map<String, InstanceFactory<MenuItem>> factories;

        StubInstanceManager() {
            factories = new HashMap<>();
            factories.put("one", new MenuItemOneFactory());
            factories.put("two", new MenuItemTwoFactory());
            factories.put("three", new MenuItemThreeFactory());
            factories.put("four", new MenuItemFourFactory());
            factories.put("five", new MenuItemFiveFactory());
        }

        @Override public <T> InstanceFactory<T> getOptionalFactory(
            Class<T> type, String classifier, FactoryFilter factoryFilter
        ) {
            //noinspection unchecked
            return (InstanceFactory<T>) factories.get(classifier);
        }
        @Override public <T> List<InstanceFactory<T>> getManyFactories(
            Class<T> type, String classifier, FactoryFilter factoryFilter
        ) {
            throw new UnsupportedOperationException();
        }
    }

    private interface MenuItem {}

    private static class MenuItemOne implements MenuItem {}
    private static class MenuItemTwo implements MenuItem {}
    private static class MenuItemThree implements MenuItem {}
    private static class MenuItemFour implements MenuItem {}

    private static class MenuItemFive implements MenuItem {
        public MenuItemFive(InstanceScope scope) {
            scope.getSingle(MenuItem.class, "five");
        }
    }

}
