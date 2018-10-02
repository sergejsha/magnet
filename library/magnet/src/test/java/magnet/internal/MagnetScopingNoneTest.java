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

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import magnet.Scope;
import magnet.Scoping;

public class MagnetScopingNoneTest {

    private InstrumentedScope scope1;
    private InstrumentedScope scope2;
    private InstrumentedScope scope3;

    @Before
    public void before() {
        InstanceManager instanceManager = new StubInstanceManager();
        scope1 = (InstrumentedScope) new InstrumentedScope(
                new MagnetScope(null, instanceManager))
                .bind(Dependency1.class, new Dependency1());

        scope2 = (InstrumentedScope) scope1
                .createSubscope()
                .bind(Dependency2.class, new Dependency2());

        scope3 = (InstrumentedScope) scope2
                .createSubscope()
                .bind(Dependency3.class, new Dependency3());
    }

    @Test
    public void itemOne_requestedWithinScope1() {
        // when
        MenuItem menuItem = scope1.getSingle(MenuItem.class, "one");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope3.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope1.getOptionalInScope(MenuItem.class, "one")).isNull();
    }

    @Test
    public void itemOne_requestedWithinScope2() {
        // when
        MenuItem menuItem = scope2.getSingle(MenuItem.class, "one");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope3.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope1.getOptionalInScope(MenuItem.class, "one")).isNull();
    }

    @Test
    public void itemOne_requestedWithinScope3() {
        // when
        MenuItem menuItem = scope3.getSingle(MenuItem.class, "one");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope3.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope1.getOptionalInScope(MenuItem.class, "one")).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void itemTwo_requestedWithinScope1() {
        // when
        scope1.getSingle(MenuItem.class, "two");
    }

    @Test
    public void itemTwo_requestedWithinScope2() {
        // when
        MenuItem menuItem = scope2.getSingle(MenuItem.class, "two");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope3.getOptionalInScope(MenuItem.class, "two")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "two")).isEqualTo(menuItem);
        assertThat(scope1.getOptionalInScope(MenuItem.class, "two")).isNull();

        assertThat(scope3.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope1.getOptionalInScope(MenuItem.class, "one")).isNull();
    }

    @Test
    public void itemTwo_requestedWithinScope3() {
        // when
        MenuItem menuItem = scope3.getSingle(MenuItem.class, "two");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope3.getOptionalInScope(MenuItem.class, "two")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "two")).isEqualTo(menuItem);
        assertThat(scope1.getOptionalInScope(MenuItem.class, "two")).isNull();

        assertThat(scope3.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope1.getOptionalInScope(MenuItem.class, "one")).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void itemThree_requestedWithinScope1() {
        // when
        scope1.getSingle(MenuItem.class, "three");
    }

    @Test(expected = IllegalStateException.class)
    public void itemThree_requestedWithinScope2() {
        // when
        scope2.getSingle(MenuItem.class, "three");
    }

    @Test
    public void itemThree_requestedWithinScope3() {
        // when
        MenuItem menuItem = scope3.getSingle(MenuItem.class, "three");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope3.getOptionalInScope(MenuItem.class, "two")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "two")).isNotNull();
        assertThat(scope1.getOptionalInScope(MenuItem.class, "two")).isNull();

        assertThat(scope3.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope1.getOptionalInScope(MenuItem.class, "one")).isNull();
    }

    private static class MenuItemOneFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(Dependency1.class);
            return new MenuItemOne();
        }
        @Override public Scoping getScoping() { return Scoping.UNSCOPED; }
    }

    private static class MenuItemTwoFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(Dependency1.class);
            scope.getSingle(Dependency2.class);
            scope.getSingle(MenuItem.class, "one");
            return new MenuItemTwo();
        }
        @Override public Scoping getScoping() { return Scoping.TOPMOST; }
    }

    private static class MenuItemThreeFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(Dependency1.class);
            scope.getSingle(Dependency3.class);
            scope.getSingle(MenuItem.class, "one");
            scope.getSingle(MenuItem.class, "two");
            return new MenuItemThree();
        }
        @Override public Scoping getScoping() { return Scoping.UNSCOPED; }
    }

    private static class StubInstanceManager implements InstanceManager {
        private final Map<String, InstanceFactory<MenuItem>> factories;

        StubInstanceManager() {
            factories = new HashMap<>();
            factories.put("one", new MenuItemOneFactory());
            factories.put("two", new MenuItemTwoFactory());
            factories.put("three", new MenuItemThreeFactory());
        }

        @Override public <T> InstanceFactory<T> getOptionalFactory(Class<T> type, String classifier) {
            //noinspection unchecked
            return (InstanceFactory<T>) factories.get(classifier);
        }
        @Override public <T> List<InstanceFactory<T>> getManyFactories(Class<T> type, String classifier) {
            throw new UnsupportedOperationException();
        }
    }

    private interface MenuItem {}
    private static class MenuItemOne implements MenuItem {}
    private static class MenuItemTwo implements MenuItem {}
    private static class MenuItemThree implements MenuItem {}

    private static class Dependency1 {}
    private static class Dependency2 {}
    private static class Dependency3 {}
}
