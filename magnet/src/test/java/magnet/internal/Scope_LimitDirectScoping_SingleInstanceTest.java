package magnet.internal;

import magnet.Scope;
import magnet.Scoping;
import magnet.internal.observer.ScopeObserver;
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

@RunWith(JUnit4.class)
public class Scope_LimitDirectScoping_SingleInstanceTest {

    // Scopes: A <- B<limit> <- C
    // Instances: Dep1<limit>(DIRECT)
    // When: getSingle<Dep1>
    // Expected: A {}, B {Dep1}, C {}

    @Rule
    public ExpectedException expected = ExpectedException.none();

    private final static String LIMIT = "limit";

    @Mock private Scope scopeA;
    @Mock private Scope scopeB;
    @Mock private Scope scopeC;

    @Test
    public void test_GetFromUnderLimitedScope() {
        // given
        scopeA = InternalFactory.createRootScope(new HashMapInstanceManager());
        scopeB = scopeA.createSubscope().limit(LIMIT);
        scopeC = scopeB.createSubscope();

        // when
        scopeC.getSingle(Dep1.class);

        // then
        ScopeObserver observer = new ScopeObserver();
        scopeA.accept(observer, Integer.MAX_VALUE);
        observer.assetThat(scopeA).hasNoInstances();
        observer.assetThat(scopeB).hasInstanceTypes(Dep1.class);
        observer.assetThat(scopeC).hasNoInstances();
    }

    @Test
    public void test_GetFromLimitedScope() {
        // given
        scopeA = InternalFactory.createRootScope(new HashMapInstanceManager());
        scopeB = scopeA.createSubscope().limit(LIMIT);
        scopeC = scopeB.createSubscope();

        // when
        scopeB.getSingle(Dep1.class);

        // then
        ScopeObserver observer = new ScopeObserver();
        scopeA.accept(observer, Integer.MAX_VALUE);
        observer.assetThat(scopeA).hasNoInstances();
        observer.assetThat(scopeB).hasInstanceTypes(Dep1.class);
        observer.assetThat(scopeC).hasNoInstances();
    }

    @Test
    public void test_GetFromAboveLimitedScope() {
        expected.expect(RuntimeException.class);
        expected.expectMessage("Cannot register instance");

        // given
        scopeA = InternalFactory.createRootScope(new HashMapInstanceManager());
        scopeB = scopeA.createSubscope().limit(LIMIT);
        scopeC = scopeB.createSubscope();

        // when
        scopeA.getSingle(Dep1.class);
    }

    @SuppressWarnings("unchecked") private static class HashMapInstanceManager implements InstanceManager {
        private HashMap<Class, InstanceFactory> factories = new HashMap<>();

        HashMapInstanceManager() {
            factories.put(Dep1.class, new Dep1Factory());
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
            throw new UnsupportedOperationException();
        }
    }

    private static class Dep1 {}
    private static class Dep1Factory extends InstanceFactory<Dep1> {
        @Override public Dep1 create(Scope scope) { return new Dep1(); }
        @Override public Scoping getScoping() { return Scoping.DIRECT; }
        @Override public String getLimit() { return LIMIT; }
    }
}
