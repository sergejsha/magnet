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

import magnet.Scope;
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
public class MagnetScopingTopmostDependsOnUnscopedTest {

    private InstrumentedScope scope1;
    private InstrumentedScope scope2;
    private InstrumentedScope scope3;

    @Before
    public void before() {
        InstanceManager instanceManager = new StubInstanceManager();

        scope1 = new InstrumentedScope(new MagnetScope(null, instanceManager));
        scope2 = (InstrumentedScope) scope1.createSubscope();
        scope3 = (InstrumentedScope) scope2.createSubscope();
    }

    private interface MenuItem {}

    private static class MenuItemZero implements MenuItem {}
    private static class MenuItemOne implements MenuItem {}
    private static class MenuItemTwo implements MenuItem {}
    private static class MenuItemThree implements MenuItem {}

    private static class MenuItemZeroFactory extends InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            return new MenuItemZero();
        }
        @Override public Scoping getScoping() { return Scoping.UNSCOPED; }
    }

    private static class MenuItemOneFactory extends InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            return new MenuItemOne();
        }
        @Override public Scoping getScoping() { return Scoping.UNSCOPED; }
    }

    private static class MenuItemTwoFactory extends InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(MenuItem.class, "one");
            return new MenuItemTwo();
        }
        @Override public Scoping getScoping() { return Scoping.UNSCOPED; }
    }

    private static class MenuItemThreeFactory extends InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(MenuItem.class, "two");
            return new MenuItemThree();
        }
        @Override public Scoping getScoping() { return Scoping.TOPMOST; }
    }

    @Test
    public void itemThree_getSingleInScope3() {
        // when
        MenuItem item = scope3.getSingle(MenuItem.class, "three");

        // then
        assertThat(item).isNotNull();
        assertThat(scope3.getOptionalInScope(MenuItem.class, "three")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "three")).isNull();
        assertThat(scope1.getOptionalInScope(MenuItem.class, "three")).isEqualTo(item);
    }

    @Test
    public void itemThree_getSingleInScope2() {
        // when
        MenuItem item = scope2.getSingle(MenuItem.class, "three");

        // then
        assertThat(item).isNotNull();
        assertThat(scope3.getOptionalInScope(MenuItem.class, "three")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "three")).isNull();
        assertThat(scope1.getOptionalInScope(MenuItem.class, "three")).isEqualTo(item);
    }

    @Test
    public void itemThree_getSingleInScope1() {
        // when
        MenuItem item = scope1.getSingle(MenuItem.class, "three");

        // then
        assertThat(item).isNotNull();
        assertThat(scope3.getOptionalInScope(MenuItem.class, "three")).isNull();
        assertThat(scope2.getOptionalInScope(MenuItem.class, "three")).isNull();
        assertThat(scope1.getOptionalInScope(MenuItem.class, "three")).isEqualTo(item);
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

        @Override
        public <T> InstanceFactory getInstanceFactory(
            Class<T> instanceType, String classifier, Class<InstanceFactory<T>> factoryType
        ) {
            throw new UnsupportedOperationException();
        }

        @Override public <T> InstanceFactory<T> getFilteredInstanceFactory(
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
    }


}
