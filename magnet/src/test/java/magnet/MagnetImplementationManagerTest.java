package magnet;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    Factory<Type2> factoryType2Impl1;

    @Mock
    Factory<Type2> factoryType2Impl2;

    @Mock
    Scope scope;

    private MagnetImplementationManager implManager;

    @Before
    public void before() {
        implManager = new MagnetImplementationManager();

        when(factoryType1Impl1.create(scope)).thenReturn(new Type1Impl());
        when(factoryType1Impl2.create(scope)).thenReturn(new Type1Impl());
        when(factoryType2Impl1.create(scope)).thenReturn(new Type2Impl());
        when(factoryType2Impl2.create(scope)).thenReturn(new Type2Impl());

        Factory[] factories = new Factory[] {
                factoryType1Impl1,
                factoryType1Impl2,
                factoryType2Impl1,
                factoryType2Impl2
        };

        Map<Class, Object> index = new HashMap<>();

        Map<String, Range> ranges1 = new HashMap<>();
        ranges1.put("impl1", new Range(0, 1, "impl1"));
        ranges1.put("impl2", new Range(1, 1, "impl2"));

        index.put(Type1.class, ranges1);
        index.put(Type2.class, new Range(2, 2, ""));

        implManager.register(factories, index);
    }

    @Test
    public void test_GetMany_UnknownType_NoTarget() {
        // when
        List<Object> impls = implManager.getMany(Object.class, scope);

        // then
        assertThat(impls).isEmpty();
    }

    @Test
    public void test_GetMany_Type_Target_default() {
        // when
        List<Type2> impls = implManager.getMany(Type2.class, scope);

        // then
        verify(factoryType2Impl1).create(scope);
        verify(factoryType2Impl2).create(scope);
        assertThat(impls).hasSize(2);
        assertThat(impls.get(0)).isNotNull();
        assertThat(impls.get(1)).isNotNull();
    }

    @Test
    public void test_GetMany_Type_Target_impl1() {
        // when
        List<Type1> impls = implManager.getMany(Type1.class, "impl1", scope);

        // then
        verify(factoryType1Impl1).create(scope);
        assertThat(impls).hasSize(1);
        assertThat(impls.get(0)).isNotNull();
    }

    @Test
    public void test_GetMany_Type_Target_impl2() {
        // when
        List<Type1> impls = implManager.getMany(Type1.class, "impl2", scope);

        // then
        verify(factoryType1Impl2).create(scope);
        assertThat(impls).hasSize(1);
        assertThat(impls.get(0)).isNotNull();
    }

    @Test
    public void test_GetSingle_OneFound() {
        // when
        Type1 impl = implManager.getSingle(Type1.class, "impl2", scope);

        // then
        assertThat(impl).isNotNull();
        verify(factoryType1Impl2).create(scope);
    }

    @Test
    public void test_GetSingle_NoneFound() {
        // when
        Type3 impl = implManager.getSingle(Type3.class, scope);

        // then
        assertThat(impl).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void test_GetSingle_MultipleFound() {
        implManager.getSingle(Type2.class, scope);
    }

    @Test
    public void test_RequireSingle_OneFound() {
        // when
        Type1 impl = implManager.requireSingle(Type1.class, "impl2", scope);

        // then
        assertThat(impl).isNotNull();
        verify(factoryType1Impl2).create(scope);
    }

    @Test(expected = IllegalStateException.class)
    public void test_RequireSingle_NoneFound() {
        implManager.requireSingle(Type3.class, scope);
    }

    @Test(expected = IllegalStateException.class)
    public void test_RequireSingle_MultipleFound() {
        implManager.requireSingle(Type2.class, scope);
    }

    interface Type1 {}

    interface Type2 {}

    interface Type3 {}

    class Type1Impl implements Type1 {}

    class Type2Impl implements Type2 {}

}
