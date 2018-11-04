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

import magnet.Classifier;
import magnet.ScopeContainer;
import magnet.Scoping;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MagnetScope_SiblingTypesTest {

    @Test
    public void sameInstanceIsRegisteredScope() {
        MagnetInstanceManager instanceManager = createMagnetInstanceManager(Scoping.TOPMOST);
        ScopeContainer topMost = new MagnetScopeContainer(null, instanceManager);
        ScopeContainer directScope = topMost.createSubscope();

        Interface1 instance1 = directScope.getSingle(Interface1.class);
        Interface2 instance2 = directScope.getSingle(Interface2.class);
        assertThat(instance1).isSameAs(instance2);
    }

    @Test
    public void sameInstanceIsRegisteredScopeInReverseOrder() {
        MagnetInstanceManager instanceManager = createMagnetInstanceManager(Scoping.TOPMOST);
        ScopeContainer topMost = new MagnetScopeContainer(null, instanceManager);
        ScopeContainer directScope = topMost.createSubscope();

        Interface2 instance2 = directScope.getSingle(Interface2.class);
        Interface1 instance1 = directScope.getSingle(Interface1.class);
        assertThat(instance1).isSameAs(instance2);
    }

    @Test
    public void sameInstanceIsRegisteredInTopMostScope() {
        MagnetInstanceManager instanceManager = createMagnetInstanceManager(Scoping.TOPMOST);
        ScopeContainer topMost = new MagnetScopeContainer(null, instanceManager);
        ScopeContainer directScope = topMost.createSubscope();
        directScope.getSingle(Interface1.class);

        Interface1 instance1 = topMost.getSingle(Interface1.class);
        Interface2 instance2 = topMost.getSingle(Interface2.class);
        assertThat(instance1).isSameAs(instance2);
    }

    @Test
    public void sameInstanceIsRegisteredInDirectScope() {
        MagnetInstanceManager instanceManager = createMagnetInstanceManager(Scoping.DIRECT);
        ScopeContainer topMost = new MagnetScopeContainer(null, instanceManager);
        ScopeContainer directScope = topMost.createSubscope();

        Interface1 instance1 = directScope.getSingle(Interface1.class);
        Interface2 instance2 = directScope.getSingle(Interface2.class);
        assertThat(instance1).isSameAs(instance2);
    }

    @Test
    public void sameInstanceIsNotRegisteredInTopMostScope() {
        MagnetInstanceManager instanceManager = createMagnetInstanceManager(Scoping.DIRECT);
        MagnetScopeContainer topMost = new MagnetScopeContainer(null, instanceManager);
        ScopeContainer directScope = topMost.createSubscope();
        directScope.getSingle(Interface1.class);

        assertThat(topMost.instances).isEmpty();
    }

    private static MagnetInstanceManager createMagnetInstanceManager(Scoping scoping) {
        MagnetInstanceManager instanceManager = new MagnetInstanceManager();

        InstanceFactory[] factories = new InstanceFactory[]{
            new ImplementationInterface1Factory(scoping),
            new ImplementationInterface2Factory(scoping)
        };

        Map<Class, Object> index = new HashMap<>();
        index.put(Interface1.class, new Range(0, 1, Classifier.NONE));
        index.put(Interface2.class, new Range(1, 1, Classifier.NONE));

        instanceManager.register(factories, index, null);
        return instanceManager;
    }

    interface Interface1 {}
    interface Interface2 {}

    static class Implementation implements Interface1, Interface2 {}

    static class ImplementationInterface1Factory extends InstanceFactory<Interface1> {
        private Scoping scoping;
        ImplementationInterface1Factory(Scoping scoping) { this.scoping = scoping; }
        @Override public Interface1 create(ScopeContainer scope) { return new Implementation(); }
        @Override public Scoping getScoping() { return this.scoping; }
        @Override public Class[] getSiblingTypes() {
            return new Class[]{Interface2.class, ImplementationInterface2Factory.class};
        }
    }

    static class ImplementationInterface2Factory extends InstanceFactory<Interface2> {
        private Scoping scoping;
        ImplementationInterface2Factory(Scoping scoping) { this.scoping = scoping; }
        @Override public Interface2 create(ScopeContainer scope) { return new Implementation(); }
        @Override public Scoping getScoping() { return this.scoping; }
        @Override public Class[] getSiblingTypes() {
            return new Class[]{Interface1.class, ImplementationInterface1Factory.class};
        }
    }

}
