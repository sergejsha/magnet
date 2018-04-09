package magnet;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MagnetAutoScopeTest {

    private InstanceManager instanceManager;

    private MagnetScope scope0;
    private MagnetScope scope1;
    private MagnetScope scope2;

    @Before
    public void before() {

        // [scope2: dep2] -> [scope1: dep1] -> [scope0: dep0]
        // one -> [], two -> [dep1], three -> [one, dep0], four -> [dep2], five -> [one, two]

        instanceManager = new StubInstanceManager();

        scope0 = (MagnetScope) new MagnetScope(null, instanceManager).register(Dependency0.class, new Dependency0());
        scope1 = (MagnetScope) scope0.subscope().register(Dependency1.class, new Dependency1());
        scope2 = (MagnetScope) scope1.subscope().register(Dependency2.class, new Dependency2());
    }

    @Test
    public void itemOne_requestedWithinScope2() {
        // when
        MenuItem menuItemOne = scope2.getSingle(MenuItem.class, "one");

        // then
        assertThat(menuItemOne).isNotNull();
        assertThat(scope2.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope1.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope0.getScopeObject(MenuItem.class, "one")).isEqualTo(menuItemOne);
    }

    @Test
    public void itemOne_requestedWithinScope1() {
        // when
        MenuItem menuItemOne = scope1.getSingle(MenuItem.class, "one");

        // then
        assertThat(menuItemOne).isNotNull();
        assertThat(scope2.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope1.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope0.getScopeObject(MenuItem.class, "one")).isEqualTo(menuItemOne);
    }

    @Test
    public void itemOne_requestedWithinScope0() {
        // when
        MenuItem menuItemOne = scope0.getSingle(MenuItem.class, "one");

        // then
        assertThat(menuItemOne).isNotNull();
        assertThat(scope2.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope1.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope0.getScopeObject(MenuItem.class, "one")).isEqualTo(menuItemOne);
    }

    @Test
    public void itemTwo_requestedWithinScope2() {
        // when
        MenuItem menuItemTwo = scope2.getSingle(MenuItem.class, "two");

        // then
        assertThat(menuItemTwo).isNotNull();
        assertThat(scope2.getScopeObject(MenuItem.class, "two")).isNull();
        assertThat(scope1.getScopeObject(MenuItem.class, "two")).isEqualTo(menuItemTwo);
        assertThat(scope0.getScopeObject(MenuItem.class, "two")).isNull();
    }

    @Test
    public void itemTwo_requestedWithinScope1() {
        // when
        MenuItem menuItemTwo = scope1.getSingle(MenuItem.class, "two");

        // then
        assertThat(menuItemTwo).isNotNull();
        assertThat(scope2.getScopeObject(MenuItem.class, "two")).isNull();
        assertThat(scope1.getScopeObject(MenuItem.class, "two")).isEqualTo(menuItemTwo);
        assertThat(scope0.getScopeObject(MenuItem.class, "two")).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void itemTwo_requestedWithinScope0() {
        // when
        scope0.getSingle(MenuItem.class, "two");
    }

    @Test
    public void itemThree_requestedWithinScope2() {
        // when
        MenuItem menuItemThree = scope2.getSingle(MenuItem.class, "three");

        // then
        assertThat(menuItemThree).isNotNull();
        assertThat(scope2.getScopeObject(MenuItem.class, "three")).isNull();
        assertThat(scope1.getScopeObject(MenuItem.class, "three")).isNull();
        assertThat(scope0.getScopeObject(MenuItem.class, "three")).isEqualTo(menuItemThree);

        assertThat(scope2.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope1.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope0.getScopeObject(MenuItem.class, "one")).isInstanceOf(MenuItem.class);
    }

    @Test
    public void itemThree_requestedWithinScope1() {
        // when
        MenuItem menuItemThree = scope1.getSingle(MenuItem.class, "three");

        // then
        assertThat(menuItemThree).isNotNull();
        assertThat(scope2.getScopeObject(MenuItem.class, "three")).isNull();
        assertThat(scope1.getScopeObject(MenuItem.class, "three")).isNull();
        assertThat(scope0.getScopeObject(MenuItem.class, "three")).isEqualTo(menuItemThree);

        assertThat(scope2.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope1.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope0.getScopeObject(MenuItem.class, "one")).isInstanceOf(MenuItem.class);
    }

    @Test
    public void itemThree_requestedWithinScope0() {
        // when
        MenuItem menuItemThree = scope0.getSingle(MenuItem.class, "three");

        // then
        assertThat(menuItemThree).isNotNull();
        assertThat(scope2.getScopeObject(MenuItem.class, "three")).isNull();
        assertThat(scope1.getScopeObject(MenuItem.class, "three")).isNull();
        assertThat(scope0.getScopeObject(MenuItem.class, "three")).isEqualTo(menuItemThree);

        assertThat(scope2.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope1.getScopeObject(MenuItem.class, "one")).isNull();
        assertThat(scope0.getScopeObject(MenuItem.class, "one")).isInstanceOf(MenuItem.class);
    }

    @Test(expected = IllegalStateException.class)
    public void itemFour_requestedWithinScope0() {
        // when
        scope0.getSingle(MenuItem.class, "four");
    }

    @Test(expected = IllegalStateException.class)
    public void itemFour_requestedWithinScope1() {
        // when
        scope1.getSingle(MenuItem.class, "four");
    }

    @Test
    public void itemFour_requestedWithinScope2() {
        // when
        MenuItem menuItemFour = scope2.getSingle(MenuItem.class, "four");

        // then
        assertThat(menuItemFour).isNotNull();
        assertThat(scope2.getScopeObject(MenuItem.class, "four")).isEqualTo(menuItemFour);
        assertThat(scope1.getScopeObject(MenuItem.class, "four")).isNull();
        assertThat(scope0.getScopeObject(MenuItem.class, "four")).isNull();
    }

    @Test(expected = IllegalStateException.class)
    public void itemFive_requestedWithinScope0() {
        // when
        scope0.getSingle(MenuItem.class, "five");
    }

    @Test
    public void itemFive_requestedWithinScope1() {
        // when
        MenuItem menuItemFive = scope1.getSingle(MenuItem.class, "five");

        // then
        assertThat(menuItemFive).isNotNull();
        assertThat(scope2.getScopeObject(MenuItem.class, "five")).isNull();
        assertThat(scope1.getScopeObject(MenuItem.class, "five")).isEqualTo(menuItemFive);
        assertThat(scope0.getScopeObject(MenuItem.class, "five")).isNull();
    }

}

class MenuItemOneFactory implements InstanceFactory<MenuItem> {
    @Override public MenuItem create(Scope scope) {
        return new MenuItemOne();
    }
    @Override public boolean isScoped() { return true; }
}

class MenuItemTwoFactory implements InstanceFactory<MenuItem> {
    @Override public MenuItem create(Scope scope) {
        scope.getSingle(Dependency1.class);
        return new MenuItemTwo();
    }
    @Override public boolean isScoped() { return true; }
}

class MenuItemThreeFactory implements InstanceFactory<MenuItem> {
    @Override public MenuItem create(Scope scope) {
        scope.getSingle(Dependency0.class);
        scope.getSingle(MenuItem.class, "one");
        return new MenuItemThree();
    }
    @Override public boolean isScoped() { return true; }
}

class MenuItemFourFactory implements InstanceFactory<MenuItem> {
    @Override public MenuItem create(Scope scope) {
        scope.getSingle(Dependency2.class);
        return new MenuItemFour();
    }
    @Override public boolean isScoped() { return true; }
}

class MenuItemFiveFactory implements InstanceFactory<MenuItem> {
    @Override public MenuItem create(Scope scope) {
        scope.getSingle(MenuItem.class, "one");
        scope.getSingle(MenuItem.class, "two");
        return new MenuItemFive();
    }
    @Override public boolean isScoped() { return true; }
}

class StubInstanceManager implements InstanceManager {
    private final Map<String, InstanceFactory<MenuItem>> factories;

    StubInstanceManager() {
        factories = new HashMap<>();
        factories.put("one", new MenuItemOneFactory());
        factories.put("two", new MenuItemTwoFactory());
        factories.put("three", new MenuItemThreeFactory());
        factories.put("four", new MenuItemFourFactory());
        factories.put("five", new MenuItemFiveFactory());
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

interface MenuItem {}

class MenuItemOne implements MenuItem {}
class MenuItemTwo implements MenuItem {}
class MenuItemThree implements MenuItem {}
class MenuItemFour implements MenuItem {}
class MenuItemFive implements MenuItem {}

class Dependency0 {}
class Dependency1 {}
class Dependency2 {}
