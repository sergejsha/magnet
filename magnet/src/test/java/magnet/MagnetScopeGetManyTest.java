package magnet;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MagnetScopeGetManyTest {

    private MagnetScope scope1;
    private MagnetScope scope2;

    @Before
    public void before() {
        scope1 = new MagnetScope(null, new StubInstanceManager());
        scope2 = (MagnetScope) scope1.subscope().register(Dependency2.class, new Dependency2());
    }

    @Test
    public void getSingleScopedMany() {
        // when
        List<MenuItem> oneList = scope1.getMany(MenuItem.class, "one");

        // then
        assertThat(oneList).hasSize(3);
        assertThat(oneList).containsNoDuplicates();

        List<MenuItem> onesInScope = scope1.getRegisteredMany(MenuItem.class, "one");
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

        List<MenuItem> oneScope1 = scope1.getRegisteredMany(MenuItem.class, "one");
        List<MenuItem> twoScope1 = scope1.getRegisteredMany(MenuItem.class, "two");
        List<MenuItem> oneScope2 = scope2.getRegisteredMany(MenuItem.class, "one");
        List<MenuItem> twoScope2 = scope2.getRegisteredMany(MenuItem.class, "two");

        assertThat(oneScope1).hasSize(3);
        assertThat(twoScope1).hasSize(1);
        assertThat(oneScope2).hasSize(0);
        assertThat(twoScope2).hasSize(1);

        assertThat(twoList).contains(twoScope1.get(0));
        assertThat(twoList).contains(twoScope2.get(0));
    }

    private static class MenuItemOne1Factory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            return new MenuItemOne1();
        }
        @Override public InstanceRetention getInstanceRetention() { return InstanceRetention.SCOPE; }
    }

    private static class MenuItemOne2Factory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            return new MenuItemOne2();
        }
        @Override public InstanceRetention getInstanceRetention() { return InstanceRetention.SCOPE; }
    }

    private static class MenuItemOne3Factory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            return new MenuItemOne3();
        }
        @Override public InstanceRetention getInstanceRetention() { return InstanceRetention.SCOPE; }
    }

    private static class MenuItemTwo1Factory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            return new MenuItemTwo1();
        }
        @Override public InstanceRetention getInstanceRetention() { return InstanceRetention.SCOPE; }
    }

    private static class MenuItemTwo2Factory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(Dependency2.class);
            scope.getMany(MenuItem.class, "one");
            return new MenuItemTwo2();
        }
        @Override public InstanceRetention getInstanceRetention() { return InstanceRetention.SCOPE; }
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

        @Override public <T> InstanceFactory<T> getOptionalFactory(Class<T> type, String classifier) {
            return (InstanceFactory<T>) factories.get(classifier);
        }
        @Override public <T> List<InstanceFactory<T>> getManyFactories(Class<T> type, String classifier) {
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
