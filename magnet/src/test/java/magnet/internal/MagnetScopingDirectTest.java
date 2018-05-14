package magnet.internal;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import magnet.Scope;
import magnet.Scoping;
import magnet.internal.InstanceManager;
import magnet.internal.MagnetScope;

public class MagnetScopingDirectTest {

    private MagnetScope scope1;
    private MagnetScope scope2;
    private MagnetScope scope3;

    @Before
    public void before() {
        InstanceManager instanceManager = new StubInstanceManager();
        scope1 = (MagnetScope) new MagnetScope(null, instanceManager).bind(Dependency1.class, new Dependency1());
        scope2 = (MagnetScope) scope1.createSubscope().bind(Dependency2.class, new Dependency2());
        scope3 = (MagnetScope) scope2.createSubscope().bind(Dependency3.class, new Dependency3());
    }

    @Test
    public void itemOne_Scope1() {
        // when
        MenuItem menuItem = scope1.getSingle(MenuItem.class, "one");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "one")).isNotNull();
    }

    @Test
    public void itemOne_Scope2() {
        // when
        MenuItem menuItem = scope2.getSingle(MenuItem.class, "one");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "one")).isNotNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "one")).isNull();
    }

    @Test
    public void itemOne_Scope3() {
        // when
        MenuItem menuItem = scope3.getSingle(MenuItem.class, "one");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "one")).isNotNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "one")).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void itemTwo_Scope1() {
        // when
        MenuItem menuItem = scope1.getSingle(MenuItem.class, "two");
    }

    @Test
    public void itemTwo_Scope2() {
        // when
        MenuItem menuItem = scope2.getSingle(MenuItem.class, "two");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "two")).isNotNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "two")).isNull();
    }

    @Test
    public void itemTwo_Scope3() {
        // when
        MenuItem menuItem = scope3.getSingle(MenuItem.class, "two");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "two")).isNotNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "two")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "two")).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void itemThree_Scope1() {
        // when
        MenuItem menuItem = scope1.getSingle(MenuItem.class, "three");
    }

    @Test(expected = IllegalStateException.class)
    public void itemThree_Scope2() {
        // when
        MenuItem menuItem = scope2.getSingle(MenuItem.class, "three");
    }

    @Test
    public void itemThree_Scope3() {
        // when
        MenuItem menuItem = scope3.getSingle(MenuItem.class, "three");

        // then
        assertThat(menuItem).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "three")).isNotNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "three")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "three")).isNull();
    }

    private static class MenuItemOneFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(Dependency1.class);
            return new MenuItemOne();
        }
        @Override public Scoping getScoping() { return Scoping.DIRECT; }
    }

    private static class MenuItemTwoFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(MenuItem.class, "one");
            scope.getSingle(Dependency2.class);
            return new MenuItemTwo();
        }
        @Override public Scoping getScoping() { return Scoping.DIRECT; }
    }

    private static class MenuItemThreeFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(MenuItem.class, "two");
            scope.getSingle(Dependency3.class);
            return new MenuItemThree();
        }
        @Override public Scoping getScoping() { return Scoping.DIRECT; }
    }

    @SuppressWarnings("unchecked")
    private static class StubInstanceManager implements InstanceManager {
        private final Map<String, InstanceFactory<MenuItem>> factories;

        StubInstanceManager() {
            factories = new HashMap<>();
            factories.put("one", new MenuItemOneFactory());
            factories.put("two", new MenuItemTwoFactory());
            factories.put("three", new MenuItemThreeFactory());
        }

        @Override public <T> InstanceFactory<T> getOptionalFactory(Class<T> type, String classifier) {
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
