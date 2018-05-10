package magnet;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MagnetScopingTopmostDependsOnDirectTest {

    private MagnetScope scope1;
    private MagnetScope scope2;
    private MagnetScope scope3;

    @Before
    public void before() {
        InstanceManager instanceManager = new StubInstanceManager();

        scope1 = new MagnetScope(null, instanceManager);
        scope2 = (MagnetScope) scope1.createSubscope();
        scope3 = (MagnetScope) scope2.createSubscope();
    }

    private interface MenuItem {}

    private static class MenuItemZero implements MenuItem {}
    private static class MenuItemOne implements MenuItem {}
    private static class MenuItemTwo implements MenuItem {}
    private static class MenuItemThree implements MenuItem {}

    private static class MenuItemZeroFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            return new MenuItemZero();
        }
        @Override public Scoping getScoping() { return Scoping.DIRECT; }
    }

    private static class MenuItemOneFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            return new MenuItemOne();
        }
        @Override public Scoping getScoping() { return Scoping.DIRECT; }
    }

    private static class MenuItemTwoFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(MenuItem.class, "one");
            return new MenuItemTwo();
        }
        @Override public Scoping getScoping() { return Scoping.DIRECT; }
    }

    private static class MenuItemThreeFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(MenuItem.class, "two");
            return new MenuItemThree();
        }
        @Override public Scoping getScoping() { return Scoping.TOPMOST; }
    }

    @Test
    public void itemThree_getSingleInScope3() {
        // when
        MenuItem item = scope3.getSingle(MenuItem.class, "three");

        // then
        assertThat(item).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "three")).isEqualTo(item);
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "three")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "three")).isNull();
    }

    @Test
    public void itemThree_getSingleInScope2() {
        // when
        MenuItem item = scope2.getSingle(MenuItem.class, "three");

        // then
        assertThat(item).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "three")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "three")).isEqualTo(item);
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "three")).isNull();
    }

    @Test
    public void itemThree_getSingleInScope1() {
        // when
        MenuItem item = scope1.getSingle(MenuItem.class, "three");

        // then
        assertThat(item).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "three")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "three")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "three")).isEqualTo(item);
    }

    private static class StubInstanceManager implements InstanceManager {
        private final Map<String, InstanceFactory<MenuItem>> factories;

        StubInstanceManager() {
            factories = new HashMap<>();
            factories.put("zero", new MenuItemZeroFactory());
            factories.put("one", new MenuItemOneFactory());
            factories.put("two", new MenuItemTwoFactory());
            factories.put("three", new MenuItemThreeFactory());
        }

        @Override public <T> InstanceFactory<T> getOptionalFactory(Class<T> type, String classifier) {
            //noinspection unchecked
            return (InstanceFactory<T>) factories.get(classifier);
        }
        @Override public <T> List<InstanceFactory<T>> getManyFactories(Class<T> type, String classifier) {
            throw new UnsupportedOperationException();
        }
    }


}
