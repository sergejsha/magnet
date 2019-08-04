package magnet.internal;

import magnet.Scope;
import magnet.internal.observer.ScopeObserver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RunWith(JUnit4.class)
public class Scope_GetManyAnySiblingTypesTest_Issue95 {

    @Mock private Scope scopeA;
    @Mock private Scope scopeB;

    @Test
    public void test() {
        // given
        scopeA = InternalFactory
                .createRootScope(new MapInstanceManager())
                .bind(Bound1.class, new Bound1());

        scopeB = scopeA
                .createSubscope()
                .bind(Bound3.class, new Bound3());

        // when
        scopeB.getSingle(Dep2Sibling.class);
        scopeB.getSingle(Dep1.class);

        // then
        ScopeObserver observer = new ScopeObserver();
        scopeA.accept(observer, Integer.MAX_VALUE);

        observer.assetThat(scopeA).hasInstanceTypes(Bound1.class);
        observer.assetThat(scopeB).hasInstanceTypes(
                Bound3.class, Dep1.class, Dep2.class, Dep2Sibling.class, Dep3.class
        );
    }

    @SuppressWarnings("unchecked") private static class MapInstanceManager implements InstanceManager {
        private HashMap<Class, InstanceFactory> factories = new HashMap<>();

        MapInstanceManager() {
            factories.put(
                    Scope_GetManyAnySiblingTypesTest_Issue95.Dep1.class,
                    new Scope_GetManyAnySiblingTypesTest_Issue95.Dep1Factory()
            );
            factories.put(
                    Scope_GetManyAnySiblingTypesTest_Issue95.Dep2.class,
                    new Scope_GetManyAnySiblingTypesTest_Issue95.Dep2Factory()
            );
            factories.put(
                    Scope_GetManyAnySiblingTypesTest_Issue95.Dep2Sibling.class,
                    new Scope_GetManyAnySiblingTypesTest_Issue95.Dep2SiblingFactory()
            );
            factories.put(
                    Scope_GetManyAnySiblingTypesTest_Issue95.Dep3.class,
                    new Scope_GetManyAnySiblingTypesTest_Issue95.Dep3Factory()
            );
        }

        @Override @Nullable public <T> InstanceFactory<T> getInstanceFactory(
                Class<T> instanceType, String classifier, Class<InstanceFactory<T>> factoryType) {
            return factories.get(instanceType);
        }

        @Override @Nullable public <T> InstanceFactory<T> getFilteredInstanceFactory(
                Class<T> type, String classifier, FactoryFilter factoryFilter) {
            return factories.get(type);
        }

        @Override public <T> @NotNull List<InstanceFactory<T>> getManyInstanceFactories(
                Class<T> type, String classifier, FactoryFilter factoryFilter) {
            List<InstanceFactory<T>> list = new ArrayList<>();
            list.add(factories.get(type));
            return list;
        }
    }

    private static class Dep1 {}
    private static class Dep1Factory extends InstanceFactory<Dep1> {
        @Override public Dep1 create(Scope scope) {
            scope.getSingle(Bound1.class);
            scope.getMany(Dep2.class);
            return new Dep1();
        }
    }

    private interface Dep2 {}
    private static class Dep2Factory extends InstanceFactory<Dep2> {
        @Override public Dep2 create(Scope scope) {
            scope.getSingle(Dep3.class);
            return new Scope_GetManyAnySiblingTypesTest_Issue95.Dep2Impl();
        }
        @Override public Class[] getSiblingTypes() {
            return new Class[] { Dep2Sibling.class, Scope_GetManyAnySiblingTypesTest_Issue95.Dep2SiblingFactory.class };
        }
    }

    private interface Dep2Sibling {}
    private static class Dep2SiblingFactory extends InstanceFactory<Dep2Sibling> {
        @Override public Dep2Sibling create(Scope scope) {
            scope.getSingle(Dep3.class);
            return new Scope_GetManyAnySiblingTypesTest_Issue95.Dep2Impl();
        }
        @Override public Class[] getSiblingTypes() {
            return new Class[] { Dep2.class, Scope_GetManyAnySiblingTypesTest_Issue95.Dep2Factory.class };
        }
    }

    private static class Dep2Impl implements Dep2, Dep2Sibling { }

    private static class Dep3 {}
    private static class Dep3Factory extends InstanceFactory<Dep3> {
        @Override public Dep3 create(Scope scope) {
            scope.getSingle(Bound3.class);
            return new Dep3();
        }
    }

    private static class Bound1 {}
    private static class Bound3 {}
}
