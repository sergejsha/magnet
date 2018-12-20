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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MagnetScope_FindDeepForSiblingTypesTest {

    @Mock private Implementation1Factory factory1;
    @Mock private SiblingImplementation1Factory factory21;
    @Mock private SiblingImplementation2Factory factory22;
    @Mock private Implementation instance1;
    @Mock private SiblingImplementation instance2;

    private MagnetInstanceManager instanceManager;

    @Before
    public void before() {

        instanceManager = new MagnetInstanceManager();
        InstanceFactory[] factories = new InstanceFactory[]{factory1, factory21, factory22};
        Map<Class, Object> index = new HashMap<>();
        index.put(Interface.class, new Range(0, 2, Classifier.NONE));
        index.put(SiblingInterface.class, new Range(2, 1, Classifier.NONE));
        instanceManager.register(factories, index);

        when(factory1.create(any())).thenReturn(Mockito.mock(Interface.class));
        when(factory21.create(any())).thenReturn(Mockito.mock(Interface.class));
        when(factory22.create(any())).thenReturn(Mockito.mock(SiblingInterface.class));

        when(factory21.getSiblingTypes()).thenReturn(new Class[]{SiblingInterface.class, SiblingImplementation2Factory.class});
        when(factory22.getSiblingTypes()).thenReturn(new Class[]{Interface.class, SiblingImplementation1Factory.class});
    }

    @Test
    public void getMany_collectsAlsoSiblingInstancesFromScope() {
        // given
        InstrumentedInstanceScope scope0 = new InstrumentedInstanceScope(
            new MagnetScope(null, instanceManager)
        );
        scope0.instrumentObjectIntoScope(factory21, Interface.class, instance2, Classifier.NONE);
        scope0.instrumentObjectIntoScope(factory22, SiblingInterface.class, instance2, Classifier.NONE);

        InstrumentedInstanceScope scope1 = (InstrumentedInstanceScope) scope0.createSubscope();
        scope1.instrumentObjectIntoScope(factory1, Interface.class, instance1, Classifier.NONE);

        // when
        List<Interface> many = scope1.getMany(Interface.class);

        // then
        assertThat(many).containsExactly(instance1, instance2);
    }

    // -- configuration

    private interface Interface {}
    private interface SiblingInterface {}

    private static class Implementation implements Interface {}
    private static class SiblingImplementation implements Interface, SiblingInterface {}

    private abstract static class Implementation1Factory extends InstanceFactory<Interface> {}
    private abstract static class SiblingImplementation1Factory extends InstanceFactory<Interface> {}
    private abstract static class SiblingImplementation2Factory extends InstanceFactory<SiblingInterface> {}

}
