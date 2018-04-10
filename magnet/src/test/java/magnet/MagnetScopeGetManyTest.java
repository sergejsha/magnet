package magnet;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MagnetScopeGetManyTest {

    private Scope scope;

    @Before
    public void before() {
        scope = new MagnetScope(null, new StubInstanceManager());
    }

    @Test
    public void getManyScoped() {
        List<MenuItem> oneList = scope.getMany(MenuItem.class, "one");
        assertThat(oneList).hasSize(3);
        assertThat(oneList).containsNoDuplicates();
    }

    private static class MenuItemOne1Factory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            return new MenuItemOne();
        }
        @Override public boolean isScoped() { return true; }
    }

    private static class MenuItemOne2Factory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            return new MenuItemTwo();
        }
        @Override public boolean isScoped() { return true; }
    }

    private static class MenuItemOne3Factory implements InstanceFactory<MenuItem> {
        @Override public MenuItem create(Scope scope) {
            return new MenuItemThree();
        }
        @Override public boolean isScoped() { return true; }
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
        }

        @Override public <T> InstanceFactory<T> getOptionalFactory(Class<T> type, String classifier) {
            return (InstanceFactory<T>) factories.get(classifier);
        }
        @Override public <T> List<InstanceFactory<T>> getManyFactories(Class<T> type, String classifier) {
            return (List<InstanceFactory<T>>) factories.get(classifier);
        }
    }

    private interface MenuItem {}

    private static class MenuItemOne implements MenuItem {}
    private static class MenuItemTwo implements MenuItem {}
    private static class MenuItemThree implements MenuItem {}
    private static class MenuItemFour implements MenuItem {}

    private static class MenuItemFive implements MenuItem {
        MenuItemFive(Scope scope) {
            scope.getSingle(MenuItem.class, "five");
        }
    }

}
