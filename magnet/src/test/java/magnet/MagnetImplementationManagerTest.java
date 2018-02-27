package magnet;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import magnet.internal.Factory;
import magnet.internal.Range;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class MagnetImplementationManagerTest {

    @Mock
    Factory<Type1> factoryType1Impl1;

    @Mock
    Factory<Type1> factoryType1Impl2;

    @Mock
    Factory<Type1> factoryType2Impl1;

    @Mock
    Factory<Type1> factoryType2Impl2;

    @Mock
    DependencyScope dependencyScope;

    private MagnetImplementationManager registry;

    @Before
    public void before() {
        registry = new MagnetImplementationManager();

        Factory[] factories = new Factory[] {
                factoryType1Impl1,
                factoryType1Impl2,
                factoryType2Impl1,
                factoryType2Impl2,
        };

        Map<Class, Object> index = new HashMap<>();

        Map<String, Range> ranges1 = new HashMap<>();
        ranges1.put("impl1", new Range(0, 1, "impl1"));
        ranges1.put("impl2", new Range(1, 1, "impl2"));

        index.put(Type1.class, ranges1);
        index.put(Type2.class, new Range(2, 2, ""));

        registry.register(factories, index);
    }

    @Test
    public void test_Get_UnknownType_NoTarget() {
        // when
        List<Object> impls = registry.get(Object.class, dependencyScope);

        // then
        assertThat(impls).isEmpty();
    }

    @Test
    public void test_Get_Type_Target_default() {
        // when
        List<Type2> impls = registry.get(Type2.class, dependencyScope);

        // then
        verify(factoryType2Impl1).create(dependencyScope);
        verify(factoryType2Impl2).create(dependencyScope);
        assertThat(impls).hasSize(2);
    }

    @Test
    public void test_Get_Type_Target_impl1() {
        // when
        List<Type1> impls = registry.get(Type1.class, "impl1", dependencyScope);

        // then
        verify(factoryType1Impl1).create(dependencyScope);
        assertThat(impls).hasSize(1);
    }

    @Test
    public void test_Get_Type_Target_impl2() {
        // when
        List<Type1> impls = registry.get(Type1.class, "impl2", dependencyScope);

        // then
        verify(factoryType1Impl2).create(dependencyScope);
        assertThat(impls).hasSize(1);
    }

    interface Type1 {}

    interface Type2 {}

}
