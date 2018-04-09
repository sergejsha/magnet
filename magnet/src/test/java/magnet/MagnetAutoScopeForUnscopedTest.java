package magnet;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MagnetAutoScopeForUnscopedTest {

    private InstanceManager instanceManager;

    private MagnetScope scope0;
    private MagnetScope scope1;
    private MagnetScope scope2;

    @Before
    public void before() {
        instanceManager = new StubInstanceManager();
        scope0 = (MagnetScope) new MagnetScope(null, instanceManager).register(Dependency0.class, new Dependency0());
        scope1 = (MagnetScope) scope0.subscope().register(Dependency1.class, new Dependency1());
        scope2 = (MagnetScope) scope1.subscope().register(Dependency2.class, new Dependency2());
    }

    @Test
    public void itemOne_requestedWithinScope0() {
        // when
        MenuItem menuItem = scope0.getSingle(MenuItem.class, "one");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope2.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope1.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope0.getScopeObject(MenuItem.class, "one")).isNull();
    }

    @Test
    public void itemOne_requestedWithinScope1() {
        // when
        MenuItem menuItem = scope1.getSingle(MenuItem.class, "one");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope2.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope1.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope0.getScopeObject(MenuItem.class, "one")).isNull();
    }

    @Test
    public void itemOne_requestedWithinScope2() {
        // when
        MenuItem menuItem = scope2.getSingle(MenuItem.class, "one");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope2.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope1.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope0.getScopeObject(MenuItem.class, "one")).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void itemTwo_requestedWithinScope0() {
        // when
        scope0.getSingle(MenuItem.class, "two");
    }

    @Test
    public void itemTwo_requestedWithinScope1() {
        // when
        MenuItem menuItem = scope1.getSingle(MenuItem.class, "two");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope2.getScopeObject(MenuItem.class, "two")).isNull();
        assertThat(scope1.getScopeObject(MenuItem.class, "two")).isEqualTo(menuItem);
        assertThat(scope0.getScopeObject(MenuItem.class, "two")).isNull();

        assertThat(scope2.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope1.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope0.getScopeObject(MenuItem.class, "one")).isNull();
    }

    @Test
    public void itemTwo_requestedWithinScope2() {
        // when
        MenuItem menuItem = scope2.getSingle(MenuItem.class, "two");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope2.getScopeObject(MenuItem.class, "two")).isNull();
        assertThat(scope1.getScopeObject(MenuItem.class, "two")).isEqualTo(menuItem);
        assertThat(scope0.getScopeObject(MenuItem.class, "two")).isNull();

        assertThat(scope2.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope1.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope0.getScopeObject(MenuItem.class, "one")).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void itemThree_requestedWithinScope0() {
        // when
        scope0.getSingle(MenuItem.class, "three");
    }

    @Test(expected = IllegalStateException.class)
    public void itemThree_requestedWithinScope1() {
        // when
        scope1.getSingle(MenuItem.class, "three");
    }

    @Test
    public void itemThree_requestedWithinScope2() {
        // when
        MenuItem menuItem = scope2.getSingle(MenuItem.class, "three");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope2.getScopeObject(MenuItem.class, "two")).isNull();
        assertThat(scope1.getScopeObject(MenuItem.class, "two")).isNotNull();
        assertThat(scope0.getScopeObject(MenuItem.class, "two")).isNull();

        assertThat(scope2.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope1.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope0.getScopeObject(MenuItem.class, "one")).isNull();
    }

    private static class MenuItemOneFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(Dependency0.class);
            return new MenuItemOne();
        }
        @Override public boolean isScoped() { return false; }
    }

    private static class MenuItemTwoFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(Dependency0.class);
            scope.getSingle(Dependency1.class);
            scope.getSingle(MenuItem.class, "one");
            return new MenuItemTwo();
        }
        @Override public boolean isScoped() { return true; }
    }

    private static class MenuItemThreeFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(Dependency0.class);
            scope.getSingle(Dependency2.class);
            scope.getSingle(MenuItem.class, "one");
            scope.getSingle(MenuItem.class, "two");
            return new MenuItemThree();
        }
        @Override public boolean isScoped() { return false; }
    }

    private static class StubInstanceManager implements InstanceManager {
        private final Map<String, InstanceFactory<MenuItem>> factories;

        StubInstanceManager() {
            factories = new HashMap<>();
            factories.put("one", new MenuItemOneFactory());
            factories.put("two", new MenuItemTwoFactory());
            factories.put("three", new MenuItemThreeFactory());
        }

        @Override public <T> List<T> getMany(Class<T> type, Scope scope) {
            throw new UnsupportedOperationException();
        }

        @Override public <T> List<T> getMany(Class<T> type, String classifier, Scope scope) {
            throw new UnsupportedOperationException();
        }

        @Override public <T> InstanceFactory<T> getOptionalFactory(Class<T> type, String classifier) {
            //noinspection unchecked
            return (InstanceFactory<T>) factories.get(classifier);
        }
    }

    private interface MenuItem {}
    private static class MenuItemOne implements MenuItem {}
    private static class MenuItemTwo implements MenuItem {}
    private static class MenuItemThree implements MenuItem {}

    private static class Dependency0 {}
    private static class Dependency1 {}
    private static class Dependency2 {}
}
