package magnet;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MagnetScopeCircularDependencyTest {

    private MagnetScope scope;

    @Before
    public void before() {
        scope = new MagnetScope(null, new StubInstanceManager());
    }

    @Test(expected = IllegalStateException.class)
    public void dependency_One_Two_Three_One() {
        scope.getSingle(MenuItem.class, "one");
    }

    @Test(expected = IllegalStateException.class)
    public void dependency_Four_Four() {
        scope.getSingle(MenuItem.class, "four");
    }

    @Test(expected = IllegalStateException.class)
    public void dependency_Five_Constructor_Five() {
        scope.getSingle(MenuItem.class, "five");
    }

    private static class MenuItemOneFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(MenuItem.class, "two");
            return new MenuItemOne();
        }
        @Override public InstanceRetention getInstanceRetention() { return InstanceRetention.SCOPE; }
    }

    private static class MenuItemTwoFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(MenuItem.class, "three");
            return new MenuItemTwo();
        }
        @Override public InstanceRetention getInstanceRetention() { return InstanceRetention.SCOPE; }
    }

    private static class MenuItemThreeFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(MenuItem.class, "one");
            return new MenuItemThree();
        }
        @Override public InstanceRetention getInstanceRetention() { return InstanceRetention.SCOPE; }
    }

    private static class MenuItemFourFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(MenuItem.class, "four");
            return new MenuItemFour();
        }
        @Override public InstanceRetention getInstanceRetention() { return InstanceRetention.SCOPE; }
    }

    private static class MenuItemFiveFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            return new MenuItemFive(scope);
        }
        @Override public InstanceRetention getInstanceRetention() { return InstanceRetention.SCOPE; }
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

    private static class MenuItemFive implements MenuItem {
        public MenuItemFive(Scope scope) {
            scope.getSingle(MenuItem.class, "five");
        }
    }

}
