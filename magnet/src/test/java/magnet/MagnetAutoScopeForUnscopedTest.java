package magnet;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MagnetAutoScopeForUnscopedTest {

    private InstanceManager instanceManager;

    private MagnetScope scope1;
    private MagnetScope scope2;
    private MagnetScope scope3;

    @Before
    public void before() {
        instanceManager = new StubInstanceManager();
        scope1 = (MagnetScope) new MagnetScope(null, instanceManager).register(Dependency1.class, new Dependency1());
        scope2 = (MagnetScope) scope1.subscope().register(Dependency2.class, new Dependency2());
        scope3 = (MagnetScope) scope2.subscope().register(Dependency3.class, new Dependency3());
    }

    @Test
    public void itemOne_requestedWithinScope1() {
        // when
        MenuItem menuItem = scope1.getSingle(MenuItem.class, "one");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "one")).isNull();
    }

    @Test
    public void itemOne_requestedWithinScope2() {
        // when
        MenuItem menuItem = scope2.getSingle(MenuItem.class, "one");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "one")).isNull();
    }

    @Test
    public void itemOne_requestedWithinScope3() {
        // when
        MenuItem menuItem = scope3.getSingle(MenuItem.class, "one");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "one")).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void itemTwo_requestedWithinScope1() {
        // when
        scope1.getSingle(MenuItem.class, "two");
    }

    @Test
    public void itemTwo_requestedWithinScope2() {
        // when
        MenuItem menuItem = scope2.getSingle(MenuItem.class, "two");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "two")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "two")).isEqualTo(menuItem);
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "two")).isNull();

        assertThat(scope3.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "one")).isNull();
    }

    @Test
    public void itemTwo_requestedWithinScope3() {
        // when
        MenuItem menuItem = scope3.getSingle(MenuItem.class, "two");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "two")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "two")).isEqualTo(menuItem);
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "two")).isNull();

        assertThat(scope3.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "one")).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void itemThree_requestedWithinScope1() {
        // when
        scope1.getSingle(MenuItem.class, "three");
    }

    @Test(expected = IllegalStateException.class)
    public void itemThree_requestedWithinScope2() {
        // when
        scope2.getSingle(MenuItem.class, "three");
    }

    @Test
    public void itemThree_requestedWithinScope3() {
        // when
        MenuItem menuItem = scope3.getSingle(MenuItem.class, "three");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "two")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "two")).isNotNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "two")).isNull();

        assertThat(scope3.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "one")).isNull();
    }

    private static class MenuItemOneFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(Dependency1.class);
            return new MenuItemOne();
        }
        @Override public Scoping getScoping() { return Scoping.NONE; }
    }

    private static class MenuItemTwoFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(Dependency1.class);
            scope.getSingle(Dependency2.class);
            scope.getSingle(MenuItem.class, "one");
            return new MenuItemTwo();
        }
        @Override public Scoping getScoping() { return Scoping.SCOPE; }
    }

    private static class MenuItemThreeFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(Dependency1.class);
            scope.getSingle(Dependency3.class);
            scope.getSingle(MenuItem.class, "one");
            scope.getSingle(MenuItem.class, "two");
            return new MenuItemThree();
        }
        @Override public Scoping getScoping() { return Scoping.NONE; }
    }

    private static class StubInstanceManager implements InstanceManager {
        private final Map<String, InstanceFactory<MenuItem>> factories;

        StubInstanceManager() {
            factories = new HashMap<>();
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

    private interface MenuItem {}
    private static class MenuItemOne implements MenuItem {}
    private static class MenuItemTwo implements MenuItem {}
    private static class MenuItemThree implements MenuItem {}

    private static class Dependency1 {}
    private static class Dependency2 {}
    private static class Dependency3 {}
}
