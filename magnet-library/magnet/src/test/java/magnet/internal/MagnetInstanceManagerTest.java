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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MagnetInstanceManagerTest {

    private static final String CLASSIFIER_LOCAL = "local";

    @Mock InstanceFactory<Type1> instanceFactoryType1Impl1;
    @Mock InstanceFactory<Type1> instanceFactoryType1Impl2;
    @Mock InstanceFactory<Type2> instanceFactoryType2Impl1;
    @Mock InstanceFactory<Type2> instanceFactoryType2Impl2;
    @Mock InstanceFactory<Type3> instanceFactoryType3Impl1;
    @Mock FactoryFilter factoryFilter;
    @Mock ScopeContainer scope;

    private MagnetInstanceManager instManager;

    @Before
    public void before() {
        instManager = new MagnetInstanceManager();

        when(factoryFilter.filter(any())).thenReturn(true);

        when(instanceFactoryType1Impl1.create(scope)).thenReturn(new Type1Impl());
        when(instanceFactoryType1Impl2.create(scope)).thenReturn(new Type1Impl());
        when(instanceFactoryType2Impl1.create(scope)).thenReturn(new Type2Impl());
        when(instanceFactoryType2Impl2.create(scope)).thenReturn(new Type2Impl());
        when(instanceFactoryType3Impl1.create(scope)).thenReturn(new Type3Impl());

        InstanceFactory[] factories = new InstanceFactory[]{
            instanceFactoryType1Impl1,
            instanceFactoryType1Impl2,
            instanceFactoryType2Impl1,
            instanceFactoryType2Impl2,
            instanceFactoryType3Impl1
        };

        Map<Class, Object> index = new HashMap<>();

        Map<String, Range> ranges1 = new HashMap<>();
        ranges1.put(Classifier.NONE, new Range(0, 1, Classifier.NONE));
        ranges1.put(CLASSIFIER_LOCAL, new Range(1, 1, CLASSIFIER_LOCAL));

        index.put(Type1.class, ranges1);
        index.put(Type2.class, new Range(2, 2, Classifier.NONE));
        index.put(Type3.class, new Range(4, 1, CLASSIFIER_LOCAL));

        instManager.register(factories, index, null);
    }

    @Test
    public void getOptionalFactory_Classified_Existing_SingleTypeInstance() {
        // when
        InstanceFactory<Type3> factory = instManager.getOptionalInstanceFactory(
            Type3.class, CLASSIFIER_LOCAL, factoryFilter);

        // then
        assertThat(factory).isNotNull();
    }

    @Test
    public void getOptionalFactory_Classified_Existing_ManyTypeInstances() {
        // when
        InstanceFactory<Type1> factory = instManager.getOptionalInstanceFactory(
            Type1.class, CLASSIFIER_LOCAL, factoryFilter);

        // then
        assertThat(factory).isNotNull();
    }

    @Test
    public void getOptionalFactory_NotClassified_Existing() {
        // when
        InstanceFactory<Type1> factory = instManager.getOptionalInstanceFactory(
            Type1.class, Classifier.NONE, factoryFilter);

        // then
        assertThat(factory).isNotNull();
    }

    @Test
    public void getOptionalFactory_Classified_NotExisting() {
        // when
        InstanceFactory<String> factory = instManager.getOptionalInstanceFactory(
            String.class, CLASSIFIER_LOCAL, factoryFilter);

        // then
        assertThat(factory).isNull();
    }

    @Test
    public void getOptionalFactory_NotClassified_NotExisting() {
        // when
        InstanceFactory<String> factory = instManager.getOptionalInstanceFactory(
            String.class, Classifier.NONE, factoryFilter);

        // then
        assertThat(factory).isNull();
    }

    @Test
    public void getManyFactories_NotClassified_ManyTypeInstances() {
        // when
        List<InstanceFactory<Type1>> factories = instManager.getManyInstanceFactories(
            Type1.class, Classifier.NONE, factoryFilter);

        // then
        assertThat(factories).hasSize(1);
        assertThat(factories.get(0)).isEqualTo(instanceFactoryType1Impl1);
    }

    @Test
    public void getManyFactories_Classified_ManyTypeInstances() {
        // when
        List<InstanceFactory<Type1>> factories = instManager.getManyInstanceFactories(
            Type1.class, CLASSIFIER_LOCAL, factoryFilter);

        // then
        assertThat(factories).hasSize(1);
        assertThat(factories.get(0)).isEqualTo(instanceFactoryType1Impl2);
    }

    @Test
    public void getManyFactories_NotClassified_SingleTypeInstances() {
        // when
        List<InstanceFactory<Type2>> factories = instManager.getManyInstanceFactories(
            Type2.class, Classifier.NONE, factoryFilter);

        // then
        assertThat(factories).hasSize(2);
        assertThat(factories).containsAllOf(instanceFactoryType2Impl1, instanceFactoryType2Impl2);
    }

    @Test
    public void getManyFactories_Classified_SingleTypeInstances() {
        // when
        List<InstanceFactory<Type3>> factories = instManager.getManyInstanceFactories(
            Type3.class, CLASSIFIER_LOCAL, factoryFilter);

        // then
        assertThat(factories).hasSize(1);
        assertThat(factories.get(0)).isEqualTo(instanceFactoryType3Impl1);
    }

    interface Type1 {}
    interface Type2 {}
    interface Type3 {}

    class Type1Impl implements Type1 {}
    class Type2Impl implements Type2 {}
    class Type3Impl implements Type3 {}

}
