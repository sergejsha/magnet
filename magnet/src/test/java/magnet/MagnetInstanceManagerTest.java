package magnet;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import magnet.internal.Range;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MagnetInstanceManagerTest {

    private static final String CLASSIFIER_LOCAL = "local";

    @Mock
    InstanceFactory<Type1> instanceFactoryType1Impl1;

    @Mock
    InstanceFactory<Type1> instanceFactoryType1Impl2;

    @Mock
    InstanceFactory<Type2> instanceFactoryType2Impl1;

    @Mock
    InstanceFactory<Type2> instanceFactoryType2Impl2;

    @Mock
    InstanceFactory<Type3> instanceFactoryType3Impl1;

    @Mock
    Scope scope;

    private MagnetInstanceManager implManager;

    @Before
    public void before() {
        implManager = new MagnetInstanceManager();

        when(instanceFactoryType1Impl1.create(scope)).thenReturn(new Type1Impl());
        when(instanceFactoryType1Impl2.create(scope)).thenReturn(new Type1Impl());
        when(instanceFactoryType2Impl1.create(scope)).thenReturn(new Type2Impl());
        when(instanceFactoryType2Impl2.create(scope)).thenReturn(new Type2Impl());
        when(instanceFactoryType3Impl1.create(scope)).thenReturn(new Type3Impl());

        InstanceFactory[] factories = new InstanceFactory[] {
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

        implManager.register(factories, index);
    }

    @Test
    public void getOptionalFactory_Classified_Existing_SingleTypeInstance() {
        // when
        InstanceFactory<Type3> factory = implManager.getOptionalFactory(Type3.class, CLASSIFIER_LOCAL);

        // then
        assertThat(factory).isNotNull();
    }

    @Test
    public void getOptionalFactory_Classified_Existing_ManyTypeInstances() {
        // when
        InstanceFactory<Type1> factory = implManager.getOptionalFactory(Type1.class, CLASSIFIER_LOCAL);

        // then
        assertThat(factory).isNotNull();
    }

    @Test
    public void getOptionalFactory_NotClassified_Existing() {
        // when
        InstanceFactory<Type1> factory = implManager.getOptionalFactory(Type1.class, Classifier.NONE);

        // then
        assertThat(factory).isNotNull();
    }

    @Test
    public void getOptionalFactory_Classified_NotExisting() {
        // when
        InstanceFactory<String> factory = implManager.getOptionalFactory(String.class, CLASSIFIER_LOCAL);

        // then
        assertThat(factory).isNull();
    }

    @Test
    public void getOptionalFactory_NotClassified_NotExisting() {
        // when
        InstanceFactory<String> factory = implManager.getOptionalFactory(String.class, Classifier.NONE);

        // then
        assertThat(factory).isNull();
    }

    @Test
    public void getManyFactories_NotClassified_ManyTypeInstances() {
        // when
        List<InstanceFactory<Type1>> factories = implManager.getManyFactories(Type1.class, Classifier.NONE);

        // then
        assertThat(factories).hasSize(1);
        assertThat(factories.get(0)).isEqualTo(instanceFactoryType1Impl1);
    }

    @Test
    public void getManyFactories_Classified_ManyTypeInstances() {
        // when
        List<InstanceFactory<Type1>> factories = implManager.getManyFactories(Type1.class, CLASSIFIER_LOCAL);

        // then
        assertThat(factories).hasSize(1);
        assertThat(factories.get(0)).isEqualTo(instanceFactoryType1Impl2);
    }

    @Test
    public void getManyFactories_NotClassified_SingleTypeInstances() {
        // when
        List<InstanceFactory<Type2>> factories = implManager.getManyFactories(Type2.class, Classifier.NONE);

        // then
        assertThat(factories).hasSize(2);
        assertThat(factories).containsAllOf(instanceFactoryType2Impl1, instanceFactoryType2Impl2);
    }

    @Test
    public void getManyFactories_Classified_SingleTypeInstances() {
        // when
        List<InstanceFactory<Type3>> factories = implManager.getManyFactories(Type3.class, CLASSIFIER_LOCAL);

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
