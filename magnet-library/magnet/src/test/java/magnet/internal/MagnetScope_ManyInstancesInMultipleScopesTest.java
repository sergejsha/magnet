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
import magnet.SelectorFilter;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class MagnetScope_ManyInstancesInMultipleScopesTest {

    private InstrumentedScope scope1;
    private InstrumentedScope scope2;
    private InstrumentedScope scope3;

    private InstanceImpl1Factory factory1 = new InstanceImpl1Factory();
    private InstanceImpl2Factory factory2 = new InstanceImpl2Factory();
    private InstanceImpl3Factory factory3 = new InstanceImpl3Factory();

    @Before
    public void before() {
        scope1 = new InstrumentedScope(new MagnetScope(null, new StubInstanceManager()));
        scope1.instrumentObjectIntoScope("", InstanceType.class, new InstanceImpl1(), factory1);

        scope2 = (InstrumentedScope) scope1.createSubscope();
        scope2.instrumentObjectIntoScope("", InstanceType.class, new InstanceImpl2(), factory2);

        scope3 = (InstrumentedScope) scope2.createSubscope();
    }

    @Test
    public void getSingleScopedMany() {
        // given

        // when
        List<InstanceType> instances = scope3.getMany(InstanceType.class, "");

        // then
        assertThat(instances).hasSize(3);
        assertThat(instances).containsNoDuplicates();

        assertThat(scope3.getOptionalInScope(InstanceType.class, "")).isNull();
        assertThat(scope2.getOptionalInScope(InstanceType.class, "")).isNotNull();

        List<InstanceType> instancesInScope1 = scope1.getManyInScope(InstanceType.class, "");
        assertThat(instancesInScope1).isNotNull();
        assertThat(instancesInScope1).hasSize(2);
    }

    private static class InstanceImpl1Factory extends InstanceFactory<InstanceType> {
        @Override public InstanceType create(Scope scope) { return new InstanceImpl1(); }
        @Override public Scoping getScoping() { return Scoping.TOPMOST; }
    }

    private static class InstanceImpl2Factory extends InstanceFactory<InstanceType> {
        @Override public InstanceType create(Scope scope) { return new InstanceImpl2(); }
        @Override public Scoping getScoping() { return Scoping.TOPMOST; }
    }

    private static class InstanceImpl3Factory extends InstanceFactory<InstanceType> {
        @Override public InstanceType create(Scope scope) { return new InstanceImpl3(); }
        @Override public Scoping getScoping() { return Scoping.TOPMOST; }
    }

    @SuppressWarnings("unchecked")
    private class StubInstanceManager implements InstanceManager {
        private final List instanceTypeFactories = new ArrayList<>();

        StubInstanceManager() {
            instanceTypeFactories.add(factory1);
            instanceTypeFactories.add(factory2);
            instanceTypeFactories.add(factory3);
        }

        @Override public <T> InstanceFactory<T> getOptionalFactory(Class<T> type, String classifier) {
            throw new UnsupportedOperationException();
        }
        @Override public <T> List<InstanceFactory<T>> getManyFactories(Class<T> type, String classifier) {
            if (type == InstanceType.class) {
                return (List<InstanceFactory<T>>) instanceTypeFactories;
            }
            throw new UnsupportedOperationException();
        }
        @Override public SelectorFilter getSelectorFilter(String namespace) {
            throw new UnsupportedOperationException();
        }
    }

    private interface InstanceType {}

    private static class InstanceImpl1 implements InstanceType {}
    private static class InstanceImpl2 implements InstanceType {}
    private static class InstanceImpl3 implements InstanceType {}
}
