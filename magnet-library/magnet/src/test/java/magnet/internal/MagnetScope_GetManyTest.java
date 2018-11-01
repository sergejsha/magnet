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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public class MagnetScope_GetManyTest {

    private InstrumentedInstanceScope scope1;
    private InstrumentedInstanceScope scope2;

    @Before
    public void before() {
        scope1 = new InstrumentedInstanceScope(
            new MagnetInstanceScope(null, new StubInstanceManager()));

        scope2 = (InstrumentedInstanceScope) scope1
            .createSubscope()
            .bind(Dependency2.class, new Dependency2());
    }

    @Test
    public void getSingleScopedMany() {
        // when
        List<MenuItem> oneList = scope1.getMany(MenuItem.class, "one");

        // then
        assertThat(oneList).hasSize(3);
        assertThat(oneList).containsNoDuplicates();

        List<MenuItem> onesInScope = scope1.getManyInScope(MenuItem.class, "one");
        assertThat(onesInScope).hasSize(3);
        assertThat(onesInScope).containsNoDuplicates();
        assertThat(onesInScope).containsAllIn(oneList);
    }

    @Test(expected = IllegalStateException.class)
    public void getMultiScopedMany_requestScope1() {
        scope1.getMany(MenuItem.class, "two");
    }

    @Test
    public void getMultiScopedMany_requestScope2() {
        // when
        List<MenuItem> twoList = scope2.getMany(MenuItem.class, "two");

        // then
        assertThat(twoList).hasSize(2);
        assertThat(twoList).containsNoDuplicates();

        List<MenuItem> oneScope1 = scope1.getManyInScope(MenuItem.class, "one");
        List<MenuItem> twoScope1 = scope1.getManyInScope(MenuItem.class, "two");
        List<MenuItem> oneScope2 = scope2.getManyInScope(MenuItem.class, "one");
        List<MenuItem> twoScope2 = scope2.getManyInScope(MenuItem.class, "two");

        assertThat(oneScope1).hasSize(3);
        assertThat(twoScope1).hasSize(1);
        assertThat(oneScope2).hasSize(0);
        assertThat(twoScope2).hasSize(1);

        assertThat(twoList).contains(twoScope1.get(0));
        assertThat(twoList).contains(twoScope2.get(0));
    }

    private static class MenuItemOne1Factory extends InstanceFactory<MenuItem> {
        @Override public MenuItem create(InstanceScope scope) {
            return new MenuItemOne1();
        }
        @Override public Scoping getScoping() { return Scoping.TOPMOST; }
    }

    private static class MenuItemOne2Factory extends InstanceFactory<MenuItem> {
        @Override public MenuItem create(InstanceScope scope) {
            return new MenuItemOne2();
        }
        @Override public Scoping getScoping() { return Scoping.TOPMOST; }
    }

    private static class MenuItemOne3Factory extends InstanceFactory<MenuItem> {
        @Override public MenuItem create(InstanceScope scope) {
            return new MenuItemOne3();
        }
        @Override public Scoping getScoping() { return Scoping.TOPMOST; }
    }

    private static class MenuItemTwo1Factory extends InstanceFactory<MenuItem> {
        @Override public MenuItem create(InstanceScope scope) {
            return new MenuItemTwo1();
        }
        @Override public Scoping getScoping() { return Scoping.TOPMOST; }
    }

    private static class MenuItemTwo2Factory extends InstanceFactory<MenuItem> {
        @Override public MenuItem create(InstanceScope scope) {
            scope.getSingle(Dependency2.class);
            scope.getMany(MenuItem.class, "one");
            return new MenuItemTwo2();
        }
        @Override public Scoping getScoping() { return Scoping.TOPMOST; }
    }

    @SuppressWarnings("unchecked")
    private static class StubInstanceManager implements InstanceManager {
        private final Map<String, Object> factories;

        StubInstanceManager() {
            factories = new HashMap<>();

            List<InstanceFactory> oneList = new ArrayList<>();
            oneList.add(new MenuItemOne1Factory());
            oneList.add(new MenuItemOne2Factory());
            oneList.add(new MenuItemOne3Factory());
            factories.put("one", oneList);

            List<InstanceFactory> twoList = new ArrayList<>();
            twoList.add(new MenuItemTwo1Factory());
            twoList.add(new MenuItemTwo2Factory());
            factories.put("two", twoList);
        }

        @Override public <T> InstanceFactory<T> getOptionalFactory(
            Class<T> type, String classifier, FactoryFilter factoryFilter
        ) {
            return (InstanceFactory<T>) factories.get(classifier);
        }
        @Override public <T> List<InstanceFactory<T>> getManyFactories(
            Class<T> type, String classifier, FactoryFilter factoryFilter
        ) {
            return (List<InstanceFactory<T>>) factories.get(classifier);
        }
    }

    private interface MenuItem {}

    private static class MenuItemOne1 implements MenuItem {}
    private static class MenuItemOne2 implements MenuItem {}
    private static class MenuItemOne3 implements MenuItem {}

    private static class MenuItemTwo1 implements MenuItem {}
    private static class MenuItemTwo2 implements MenuItem {}

    private static class Dependency2 {}

}
