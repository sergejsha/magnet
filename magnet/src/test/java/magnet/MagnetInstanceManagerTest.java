package magnet;

import static org.mockito.Mockito.when;

import org.junit.Before;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import magnet.internal.Range;

//@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class MagnetInstanceManagerTest {

    @Mock
    InstanceFactory<Type1> instanceFactoryType1Impl1;

    @Mock
    InstanceFactory<Type1> instanceFactoryType1Impl2;

    @Mock
    InstanceFactory<Type2> instanceFactoryType2Impl1;

    @Mock
    InstanceFactory<Type2> instanceFactoryType2Impl2;

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

        InstanceFactory[] factories = new InstanceFactory[] {
                instanceFactoryType1Impl1,
                instanceFactoryType1Impl2,
                instanceFactoryType2Impl1,
                instanceFactoryType2Impl2
        };

        Map<Class, Object> index = new HashMap<>();

        Map<String, Range> ranges1 = new HashMap<>();
        ranges1.put("impl1", new Range(0, 1, "impl1"));
        ranges1.put("impl2", new Range(1, 1, "impl2"));

        index.put(Type1.class, ranges1);
        index.put(Type2.class, new Range(2, 2, ""));

        implManager.register(factories, index);
    }

    interface Type1 {}

    interface Type2 {}

    interface Type3 {}

    class Type1Impl implements Type1 {}

    class Type2Impl implements Type2 {}

}
