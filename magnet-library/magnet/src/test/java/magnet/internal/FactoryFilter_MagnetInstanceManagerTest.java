package magnet.internal;

import magnet.Classifier;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FactoryFilter_MagnetInstanceManagerTest {

    private MagnetInstanceManager instanceManager;

    @Mock FactoryFilter factoryFilter;
    @Mock InstanceFactory<Interface1> instanceFactory1;
    @Mock InstanceFactory<Interface2> instanceFactory21;
    @Mock InstanceFactory<Interface2> instanceFactory22;

    @Before
    public void before() {
        instanceManager = new MagnetInstanceManager();
        InstanceFactory[] factories = new InstanceFactory[]{
            instanceFactory1,
            instanceFactory21,
            instanceFactory22,
        };
        Map<Class, Object> index = new HashMap<>();
        index.put(Interface1.class, new Range(0, 1, Classifier.NONE));
        index.put(Interface2.class, new Range(1, 2, Classifier.NONE));
        instanceManager.register(factories, index);
    }

    @Test
    public void getOptionalFactoryReturnsFactoryWhenFilterReturnsTrue() {
        when(factoryFilter.filter(instanceFactory1)).thenReturn(true);
        InstanceFactory<Interface1> factory = instanceManager
            .getOptionalFactory(Interface1.class, Classifier.NONE, factoryFilter);
        assertThat(factory).isSameAs(instanceFactory1);
    }

    @Test
    public void getOptionalFactoryReturnsNullWhenFilterReturnsTrue() {
        when(factoryFilter.filter(instanceFactory1)).thenReturn(false);
        InstanceFactory<Interface1> factory = instanceManager
            .getOptionalFactory(Interface1.class, Classifier.NONE, factoryFilter);
        assertThat(factory).isNull();
    }

    @Test
    public void getOptionalFactoryReturnsFactoryWhenOneOfFiltersReturnsTrue() {
        when(factoryFilter.filter(instanceFactory21)).thenReturn(false);
        when(factoryFilter.filter(instanceFactory22)).thenReturn(true);
        InstanceFactory<Interface2> factory = instanceManager
            .getOptionalFactory(Interface2.class, Classifier.NONE, factoryFilter);
        assertThat(factory).isSameAs(instanceFactory22);
    }

    @Test
    public void getOptionalFactoryReturnsNullWhenAllFiltersReturnFalse() {
        when(factoryFilter.filter(instanceFactory21)).thenReturn(false);
        when(factoryFilter.filter(instanceFactory22)).thenReturn(false);
        InstanceFactory<Interface2> factory = instanceManager
            .getOptionalFactory(Interface2.class, Classifier.NONE, factoryFilter);
        assertThat(factory).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void getOptionalFactoryFailsWhenMultipleFactoriesDetected() {
        when(factoryFilter.filter(instanceFactory21)).thenReturn(true);
        when(factoryFilter.filter(instanceFactory22)).thenReturn(true);
        instanceManager.getOptionalFactory(Interface2.class, Classifier.NONE, factoryFilter);
    }

    @Test
    public void getManyFactoriesReturnsEmptyListWhenAllFiltersReturnFalse() {
        when(instanceFactory21.getSelector()).thenReturn(new String[]{"test"});
        when(instanceFactory22.getSelector()).thenReturn(new String[]{"test"});
        when(factoryFilter.filter(instanceFactory21)).thenReturn(false);
        when(factoryFilter.filter(instanceFactory22)).thenReturn(false);

        List<InstanceFactory<Interface2>> factories = instanceManager
            .getManyFactories(Interface2.class, Classifier.NONE, factoryFilter);

        assertThat(factories).isNotNull();
        assertThat(factories).isEmpty();
    }

    @Test
    public void getManyFactoriesReturnsSingleFactoryWhenSingleFilterReturnsTrue() {
        when(instanceFactory21.getSelector()).thenReturn(new String[]{"test"});
        when(instanceFactory22.getSelector()).thenReturn(new String[]{"test"});
        when(factoryFilter.filter(instanceFactory21)).thenReturn(false);
        when(factoryFilter.filter(instanceFactory22)).thenReturn(true);

        List<InstanceFactory<Interface2>> factories = instanceManager
            .getManyFactories(Interface2.class, Classifier.NONE, factoryFilter);

        assertThat(factories).isNotNull();
        assertThat(factories).hasSize(1);
        assertThat(factories.get(0)).isSameAs(instanceFactory22);
    }

    @Test
    public void getManyFactoriesReturnsAllFactoriesWhenAllFiltersReturnTrue() {
        when(instanceFactory21.getSelector()).thenReturn(new String[]{"test"});
        when(instanceFactory22.getSelector()).thenReturn(new String[]{"test"});
        when(factoryFilter.filter(instanceFactory21)).thenReturn(true);
        when(factoryFilter.filter(instanceFactory22)).thenReturn(true);

        List<InstanceFactory<Interface2>> factories = instanceManager
            .getManyFactories(Interface2.class, Classifier.NONE, factoryFilter);

        assertThat(factories).isNotNull();
        assertThat(factories).hasSize(2);
        assertThat(factories.get(0)).isSameAs(instanceFactory21);
        assertThat(factories.get(1)).isSameAs(instanceFactory22);
    }

    @Test
    public void getManyFactoriesReturnsAllFactoriesWhenNoSelectorsDefined() {
        when(instanceFactory21.getSelector()).thenReturn(null);
        when(instanceFactory22.getSelector()).thenReturn(null);

        List<InstanceFactory<Interface2>> factories = instanceManager
            .getManyFactories(Interface2.class, Classifier.NONE, factoryFilter);

        verify(factoryFilter, never()).filter(any());
        assertThat(factories).isNotNull();
        assertThat(factories).hasSize(2);
        assertThat(factories.get(0)).isSameAs(instanceFactory21);
        assertThat(factories.get(1)).isSameAs(instanceFactory22);
    }

    @Test
    public void getManyFactoriesReturnsFactoriesWhenSomeFactoriesHaveSelectors() {
        when(instanceFactory21.getSelector()).thenReturn(null);
        when(instanceFactory22.getSelector()).thenReturn(new String[]{"test"});
        when(factoryFilter.filter(instanceFactory22)).thenReturn(true);

        List<InstanceFactory<Interface2>> factories = instanceManager
            .getManyFactories(Interface2.class, Classifier.NONE, factoryFilter);

        verify(factoryFilter, never()).filter(instanceFactory21);
        assertThat(factories).isNotNull();
        assertThat(factories).hasSize(1);
        assertThat(factories.get(0)).isSameAs(instanceFactory22);
    }

    interface Interface1 {}
    interface Interface2 {}

}
