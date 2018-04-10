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
        @Override public boolean isScoped() { return true; }
    }

    private static class MenuItemTwoFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(MenuItem.class, "three");
            return new MenuItemTwo();
        }
        @Override public boolean isScoped() { return true; }
    }

    private static class MenuItemThreeFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(MenuItem.class, "one");
            return new MenuItemThree();
        }
        @Override public boolean isScoped() { return true; }
    }

    private static class MenuItemFourFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            scope.getSingle(MenuItem.class, "four");
            return new MenuItemFour();
        }
        @Override public boolean isScoped() { return true; }
    }

    private static class MenuItemFiveFactory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            return new MenuItemFive(scope);
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
            factories.put("five", new MenuItemFourFactory());
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
    private static class MenuItemFour implements MenuItem {}

    private static class MenuItemFive implements MenuItem {
        public MenuItemFive(Scope scope) {
            scope.getSingle(MenuItem.class, "five");
        }
    }

}
