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

import magnet.ScopeContainer;
import magnet.Scoping;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public class MagnetScopingTopmostDependsOnTopmostTest {

    private InstrumentedInstanceScope scope1;
    private InstrumentedInstanceScope scope2;
    private InstrumentedInstanceScope scope3;

    @Before
    public void before() {
        InstanceManager instanceManager = new StubInstanceManager();

        scope1 = (InstrumentedInstanceScope) new InstrumentedInstanceScope(
            new MagnetScopeContainer(null, instanceManager))
            .bind(Dependency1.class, new Dependency1());

        scope2 = (InstrumentedInstanceScope) scope1
            .createSubscope()
            .bind(Dependency2.class, new Dependency2());

        scope3 = (InstrumentedInstanceScope) scope2
            .createSubscope()
            .bind(Dependency3.class, new Dependency3());
    }

    private interface MenuItem {}

    private static class MenuItemZero implements MenuItem {}
    private static class MenuItemOne implements MenuItem {}
    private static class MenuItemTwo implements MenuItem {}
    private static class MenuItemThree implements MenuItem {}

    private static class Dependency1 {}
    private static class Dependency2 {}
    private static class Dependency3 {}

    private static class MenuItemZeroFactory extends InstanceFactory<MenuItem> {
        @Override public MenuItem create(ScopeContainer scope) {
            return new MenuItemZero();
        }
        @Override public Scoping getScoping() { return Scoping.TOPMOST; }
    }

    private static class MenuItemOneFactory extends InstanceFactory<MenuItem> {
        @Override public MenuItem create(ScopeContainer scope) {
            scope.getSingle(Dependency1.class);
            return new MenuItemOne();
        }
        @Override public Scoping getScoping() { return Scoping.TOPMOST; }
    }

    private static class MenuItemTwoFactory extends InstanceFactory<MenuItem> {
        @Override public MenuItem create(ScopeContainer scope) {
            scope.getSingle(Dependency2.class);
            scope.getSingle(MenuItem.class, "one");
            return new MenuItemTwo();
        }
        @Override public Scoping getScoping() { return Scoping.TOPMOST; }
    }

    private static class MenuItemThreeFactory extends InstanceFactory<MenuItem> {
        @Override public MenuItem create(ScopeContainer scope) {
            scope.getSingle(Dependency3.class);
            scope.getSingle(MenuItem.class, "two");
            return new MenuItemThree();
        }
        @Override public Scoping getScoping() { return Scoping.TOPMOST; }
    }

    @Test
    public void itemZero_getSingleInScope3() {
        // when
        MenuItem item = scope3.getSingle(MenuItem.class, "zero");

        // then
        assertThat(item).isNotNull();
        assertThat(scope3.getOptionalInScope(MenuItem.class, "zero")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "zero")).isNull();
        assertThat(scope1.getOptionalInScope(MenuItem.class, "zero")).isEqualTo(item);
    }

    @Test
    public void itemZero_getSingleInScope2() {
        // when
        MenuItem item = scope2.getSingle(MenuItem.class, "zero");

        // then
        assertThat(item).isNotNull();
        assertThat(scope3.getOptionalInScope(MenuItem.class, "zero")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "zero")).isNull();
        assertThat(scope1.getOptionalInScope(MenuItem.class, "zero")).isEqualTo(item);
    }

    @Test
    public void itemZero_getSingleInScope1() {
        // when
        MenuItem item = scope1.getSingle(MenuItem.class, "zero");

        // then
        assertThat(item).isNotNull();
        assertThat(scope3.getOptionalInScope(MenuItem.class, "zero")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "zero")).isNull();
        assertThat(scope1.getOptionalInScope(MenuItem.class, "zero")).isEqualTo(item);
    }

    @Test
    public void itemOne_getSingleInScope3() {
        // when
        MenuItem item = scope3.getSingle(MenuItem.class, "one");

        // then
        assertThat(item).isNotNull();
        assertThat(scope3.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope1.getOptionalInScope(MenuItem.class, "one")).isEqualTo(item);
    }

    @Test
    public void itemOne_getSingleInScope2() {
        // when
        MenuItem item = scope2.getSingle(MenuItem.class, "one");

        // then
        assertThat(item).isNotNull();
        assertThat(scope3.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope1.getOptionalInScope(MenuItem.class, "one")).isEqualTo(item);
    }

    @Test
    public void itemOne_getSingleInScope1() {
        // when
        MenuItem item = scope1.getSingle(MenuItem.class, "one");

        // then
        assertThat(item).isNotNull();
        assertThat(scope3.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope1.getOptionalInScope(MenuItem.class, "one")).isEqualTo(item);
    }

    @Test
    public void itemTwo_getSingleInScope3() {
        // when
        MenuItem item = scope3.getSingle(MenuItem.class, "two");

        // then
        assertThat(item).isNotNull();
        assertThat(scope3.getOptionalInScope(MenuItem.class, "two")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "two")).isEqualTo(item);
        assertThat(scope1.getOptionalInScope(MenuItem.class, "two")).isNull();

        assertThat(scope3.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope1.getOptionalInScope(MenuItem.class, "one")).isNotNull();
    }

    @Test
    public void itemTwo_getSingleInScope2() {
        // when
        MenuItem item = scope2.getSingle(MenuItem.class, "two");

        // then
        assertThat(item).isNotNull();
        assertThat(scope3.getOptionalInScope(MenuItem.class, "two")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "two")).isEqualTo(item);
        assertThat(scope1.getOptionalInScope(MenuItem.class, "two")).isNull();

        assertThat(scope3.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope1.getOptionalInScope(MenuItem.class, "one")).isNotNull();
    }

    @Test(expected = IllegalStateException.class)
    public void itemTwo_getSingleInScope1() {
        // when
        scope1.getSingle(MenuItem.class, "two");
    }

    @Test
    public void itemThree_getSingleInScope3() {
        // when
        MenuItem item = scope3.getSingle(MenuItem.class, "three");

        // then
        assertThat(item).isNotNull();
        assertThat(scope3.getOptionalInScope(MenuItem.class, "three")).isEqualTo(item);
        assertThat(scope2.getOptionalInScope(MenuItem.class, "three")).isNull();
        assertThat(scope1.getOptionalInScope(MenuItem.class, "three")).isNull();

        assertThat(scope3.getOptionalInScope(MenuItem.class, "two")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "two")).isNotNull();
        assertThat(scope1.getOptionalInScope(MenuItem.class, "two")).isNull();

        assertThat(scope3.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "one")).isNull();
        assertThat(scope1.getOptionalInScope(MenuItem.class, "one")).isNotNull();
    }

    @Test(expected = IllegalStateException.class)
    public void itemThree_getSingleInScope2() {
        // when
        scope2.getSingle(MenuItem.class, "three");
    }

    @Test(expected = IllegalStateException.class)
    public void itemThree_getSingleInScope1() {
        // when
        scope1.getSingle(MenuItem.class, "three");
    }

    private static class StubInstanceManager implements InstanceManager {
        private final Map<String, InstanceFactory<MenuItem>> factories;

        StubInstanceManager() {
            factories = new HashMap<>();
            factories.put("zero", new MenuItemZeroFactory());
            factories.put("one", new MenuItemOneFactory());
            factories.put("two", new MenuItemTwoFactory());
            factories.put("three", new MenuItemThreeFactory());
        }

        @Override public <T> InstanceFactory<T> getOptionalInstanceFactory(
            Class<T> type, String classifier, FactoryFilter factoryFilter
        ) {
            //noinspection unchecked
            return (InstanceFactory<T>) factories.get(classifier);
        }
        @Override public <T> List<InstanceFactory<T>> getManyInstanceFactories(
            Class<T> type, String classifier, FactoryFilter factoryFilter
        ) {
            throw new UnsupportedOperationException();
        }
        @Override public <T> ScopeFactory<T> getScopeFactory(Class<T> scopeType) {
            throw new UnsupportedOperationException();
        }
    }


}
