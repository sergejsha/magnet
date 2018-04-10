package magnet;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MagnetAutoScopeForScopedTest {

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
    public void itemOne_requestedWithinScope3() {
        // when
        MenuItem menuItemOne = scope3.getSingle(MenuItem.class, "one");

        // then
        assertThat(menuItemOne).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "one")).isEqualTo(menuItemOne);
    }

    @Test
    public void itemOne_requestedWithinScope2() {
        // when
        MenuItem menuItemOne = scope2.getSingle(MenuItem.class, "one");

        // then
        assertThat(menuItemOne).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "one")).isEqualTo(menuItemOne);
    }

    @Test
    public void itemOne_requestedWithinScope1() {
        // when
        MenuItem menuItemOne = scope1.getSingle(MenuItem.class, "one");

        // then
        assertThat(menuItemOne).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "one")).isEqualTo(menuItemOne);
    }

    @Test
    public void itemTwo_requestedWithinScope3() {
        // when
        MenuItem menuItemTwo = scope3.getSingle(MenuItem.class, "two");

        // then
        assertThat(menuItemTwo).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "two")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "two")).isEqualTo(menuItemTwo);
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "two")).isNull();
    }

    @Test
    public void itemTwo_requestedWithinScope2() {
        // when
        MenuItem menuItemTwo = scope2.getSingle(MenuItem.class, "two");

        // then
        assertThat(menuItemTwo).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "two")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "two")).isEqualTo(menuItemTwo);
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "two")).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void itemTwo_requestedWithinScope1() {
        // when
        scope1.getSingle(MenuItem.class, "two");
    }

    @Test
    public void itemThree_requestedWithinScope3() {
        // when
        MenuItem menuItemThree = scope3.getSingle(MenuItem.class, "three");

        // then
        assertThat(menuItemThree).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "three")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "three")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "three")).isEqualTo(menuItemThree);

        assertThat(scope3.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "one")).isInstanceOf(MenuItem.class);
    }

    @Test
    public void itemThree_requestedWithinScope2() {
        // when
        MenuItem menuItemThree = scope2.getSingle(MenuItem.class, "three");

        // then
        assertThat(menuItemThree).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "three")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "three")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "three")).isEqualTo(menuItemThree);

        assertThat(scope3.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "one")).isInstanceOf(MenuItem.class);
    }

    @Test
    public void itemThree_requestedWithinScope1() {
        // when
        MenuItem menuItemThree = scope1.getSingle(MenuItem.class, "three");

        // then
        assertThat(menuItemThree).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "three")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "three")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "three")).isEqualTo(menuItemThree);

        assertThat(scope3.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "one")).isInstanceOf(MenuItem.class);
    }

    @Test(expected = IllegalStateException.class)
    public void itemFour_requestedWithinScope0() {
        // when
        scope1.getSingle(MenuItem.class, "four");
    }

    @Test(expected = IllegalStateException.class)
    public void itemFour_requestedWithinScope1() {
        // when
        scope2.getSingle(MenuItem.class, "four");
    }

    @Test
    public void itemFour_requestedWithinScope2() {
        // when
        MenuItem menuItemFour = scope3.getSingle(MenuItem.class, "four");

        // then
        assertThat(menuItemFour).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "four")).isEqualTo(menuItemFour);
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "four")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "four")).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void itemFive_requestedWithinScope0() {
        // when
        scope1.getSingle(MenuItem.class, "five");
    }

    @Test
    public void itemFive_requestedWithinScope1() {
        // when
        MenuItem menuItemFive = scope2.getSingle(MenuItem.class, "five");

        // then
        assertThat(menuItemFive).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "five")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "five")).isEqualTo(menuItemFive);
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "five")).isNull();

        assertThat(scope3.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "one")).isNotNull();

        assertThat(scope3.getRegisteredSingle(MenuItem.class, "two")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "two")).isNotNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "two")).isNull();
    }

    @Test
    public void itemFive_requestedWithinScope2() {
        // when
        MenuItem menuItemFive = scope3.getSingle(MenuItem.class, "five");

        // then
        assertThat(menuItemFive).isNotNull();
        assertThat(scope3.getRegisteredSingle(MenuItem.class, "five")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "five")).isEqualTo(menuItemFive);
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "five")).isNull();

        assertThat(scope3.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "one")).isNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "one")).isNotNull();

        assertThat(scope3.getRegisteredSingle(MenuItem.class, "two")).isNull();
        assertThat(scope2.getRegisteredSingle(MenuItem.class, "two")).isNotNull();
        assertThat(scope1.getRegisteredSingle(MenuItem.class, "two")).isNull();
    }

    private static class MenuItemOneFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            return new MenuItemOne();
        }
        @Override public boolean isScoped() { return true; }
    }

    private static class MenuItemTwoFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(Dependency2.class);
            return new MenuItemTwo();
        }
        @Override public boolean isScoped() { return true; }
    }

    private static class MenuItemThreeFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(Dependency1.class);
            scope.getSingle(MenuItem.class, "one");
            return new MenuItemThree();
        }
        @Override public boolean isScoped() { return true; }
    }

    private static class MenuItemFourFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(Dependency3.class);
            return new MenuItemFour();
        }
        @Override public boolean isScoped() { return true; }
    }

    private static class MenuItemFiveFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(MenuItem.class, "one");
            scope.getSingle(MenuItem.class, "two");
            return new MenuItemFive();
        }
        @Override public boolean isScoped() { return true; }
    }

    private static class StubInstanceManager implements InstanceManager {
        private final Map<String, InstanceFactory<MenuItem>> factories;

        StubInstanceManager() {
            factories = new HashMap<>();
            factories.put("one", new MenuItemOneFactory());
            factories.put("two", new MenuItemTwoFactory());
            factories.put("three", new MenuItemThreeFactory());
            factories.put("four", new MenuItemFourFactory());
            factories.put("five", new MenuItemFiveFactory());
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
    private static class MenuItemFour implements MenuItem {}
    private static class MenuItemFive implements MenuItem {}

    private static class Dependency1 {}
    private static class Dependency2 {}
    private static class Dependency3 {}

}
