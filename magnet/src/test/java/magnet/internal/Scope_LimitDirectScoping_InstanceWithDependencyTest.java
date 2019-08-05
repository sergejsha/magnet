package magnet.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;

import magnet.Scope;
import magnet.Scoping;
import magnet.internal.observer.ScopeObserver;

@RunWith(JUnit4.class)
public class Scope_LimitDirectScoping_InstanceWithDependencyTest {

    // Scopes: A <- B<limit> <- C { Bound2 }
    // Instances: Dep2 -> { DIRECT Dep1<limit>, Bound2 }

    public @Rule ExpectedException expected = ExpectedException.none();

    private final static String LIMIT = "limit";

    private @Mock Scope scopeA;
    private @Mock Scope scopeB;
    private @Mock Scope scopeC;

    @Test
    public void test_GetFromUnderLimitedScope() {
        // given
        scopeA = InternalFactory.createRootScope(new HashMapInstanceManager());
        scopeB = scopeA.createSubscope().limit(LIMIT);
        scopeC = scopeB.createSubscope().bind(Bound2.class, new Bound2());

        // when
        scopeC.getSingle(Dep2.class);

        // then
        ScopeObserver observer = new ScopeObserver();
        scopeA.accept(observer, Integer.MAX_VALUE);
        observer.assetThat(scopeA).hasNoInstances();
        observer.assetThat(scopeB).hasInstanceTypes(Dep1.class);
        observer.assetThat(scopeC).hasInstanceTypes(Bound2.class, Dep2.class);
    }

    @Test
    public void test_GetFromLimitedScope() {
        expected.expect(IllegalStateException.class);
        expected.expectMessage("not found in scopes");

        // given
        scopeA = InternalFactory.createRootScope(new HashMapInstanceManager());
        scopeB = scopeA.createSubscope().limit(LIMIT);
        scopeC = scopeB.createSubscope().bind(Bound2.class, new Bound2());

        // when
        scopeB.getSingle(Dep2.class);
    }

    @SuppressWarnings("unchecked")
    private static class HashMapInstanceManager implements InstanceManager {
        private HashMap<Class, InstanceFactory> factories = new HashMap<>();

        HashMapInstanceManager() {
            factories.put(Dep1.class, new Dep1Factory());
            factories.put(Dep2.class, new Dep2Factory());
        }

        @Override public <T> @Nullable InstanceFactory<T> getInstanceFactory(
            Class<T> instanceType, String classifier, Class<InstanceFactory<T>> factoryType) {
            return factories.get(instanceType);
        }

        @Override public <T> @Nullable InstanceFactory<T> getFilteredInstanceFactory(
            Class<T> type, String classifier, FactoryFilter factoryFilter) {
            return factories.get(type);
        }

        @Override public <T> @NotNull List<InstanceFactory<T>> getManyInstanceFactories(
            Class<T> type, String classifier, FactoryFilter factoryFilter) {
            throw new UnsupportedOperationException();
        }
    }

    private static class Bound2 {}

    private static class Dep1 {}
    private static class Dep1Factory extends InstanceFactory<Dep1> {
        @Override public Dep1 create(Scope scope) { return new Dep1(); }
        @Override public Scoping getScoping() { return Scoping.DIRECT; }
        @Override public String getLimit() { return LIMIT; }
    }

    private static class Dep2 {}
    private static class Dep2Factory extends InstanceFactory<Dep2> {
        @Override public Dep2 create(Scope scope) {
            scope.getSingle(Dep1.class);
            scope.getSingle(Bound2.class);
            return new Dep2();
        }
    }
}
