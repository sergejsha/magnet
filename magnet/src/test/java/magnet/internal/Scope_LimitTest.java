package magnet.internal;

import magnet.Scope;
import magnet.internal.observer.ScopeObserver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.List;

@RunWith(JUnit4.class)
public class Scope_LimitTest {

    private final static String LIMIT = "limit";

    @Mock private Scope scopeA;
    @Mock private Scope scopeB;
    @Mock private Scope scopeC;

    @Test
    public void test_limitedInstance_settlesInLimitedSameScope() {
        // given
        scopeA = InternalFactory.createRootScope(new HashMapInstanceManager());
        scopeB = scopeA.createSubscope();
        scopeC = scopeB.createSubscope().limit(LIMIT);

        // when
        scopeC.getSingle(Limited.class);

        // then
        ScopeObserver observer = new ScopeObserver();
        scopeA.accept(observer, Integer.MAX_VALUE);
        observer.assetThat(scopeA).hasNoInstances();
        observer.assetThat(scopeB).hasNoInstances();
        observer.assetThat(scopeC).hasInstanceTypes(Limited.class);
    }

    @Test
    public void test_limitedInstance_settlesInLimitedUpperScope() {
        // given
        scopeA = InternalFactory.createRootScope(new HashMapInstanceManager());
        scopeB = scopeA.createSubscope().limit(LIMIT);
        scopeC = scopeB.createSubscope();

        // when
        scopeC.getSingle(Limited.class);

        // then
        ScopeObserver observer = new ScopeObserver();
        scopeA.accept(observer, Integer.MAX_VALUE);
        observer.assetThat(scopeA).hasNoInstances();
        observer.assetThat(scopeB).hasInstanceTypes(Limited.class);
        observer.assetThat(scopeC).hasNoInstances();
    }

    @Test
    public void test_limitedInstance_settlesInLimitedTopScope() {
        // given
        scopeA = InternalFactory.createRootScope(new HashMapInstanceManager()).limit(LIMIT);
        scopeB = scopeA.createSubscope();
        scopeC = scopeB.createSubscope();

        // when
        scopeC.getSingle(Limited.class);

        // then
        ScopeObserver observer = new ScopeObserver();
        scopeA.accept(observer, Integer.MAX_VALUE);
        observer.assetThat(scopeA).hasInstanceTypes(Limited.class);
        observer.assetThat(scopeB).hasNoInstances();
        observer.assetThat(scopeC).hasNoInstances();
    }

    @Test
    public void test_limitedInstance_settlesInUnlimitedTopScope() {
        // given
        scopeA = InternalFactory.createRootScope(new HashMapInstanceManager());
        scopeB = scopeA.createSubscope();
        scopeC = scopeB.createSubscope();

        // when
        scopeC.getSingle(Limited.class);

        // then
        ScopeObserver observer = new ScopeObserver();
        scopeA.accept(observer, Integer.MAX_VALUE);
        observer.assetThat(scopeA).hasInstanceTypes(Limited.class);
        observer.assetThat(scopeB).hasNoInstances();
        observer.assetThat(scopeC).hasNoInstances();
    }

    @Test
    public void test_limitedInstance_ignoresUnmatchedLimit() {
        // given
        scopeA = InternalFactory.createRootScope(new HashMapInstanceManager());
        scopeB = scopeA.createSubscope().limit("limit-two");
        scopeC = scopeB.createSubscope().limit("limit-one");

        // when
        scopeC.getSingle(Limited.class);

        // then
        ScopeObserver observer = new ScopeObserver();
        scopeA.accept(observer, Integer.MAX_VALUE);
        observer.assetThat(scopeA).hasInstanceTypes(Limited.class);
        observer.assetThat(scopeB).hasNoInstances();
        observer.assetThat(scopeC).hasNoInstances();
    }

    @Test
    public void test_unlimitedInstance_ignoresLimits_settlesInUnlimitedTopScope() {
        // given
        scopeA = InternalFactory.createRootScope(new HashMapInstanceManager());
        scopeB = scopeA.createSubscope().limit("limit-two");
        scopeC = scopeB.createSubscope().limit("limit-one");

        // when
        scopeC.getSingle(Unlimited.class);

        // then
        ScopeObserver observer = new ScopeObserver();
        scopeA.accept(observer, Integer.MAX_VALUE);
        observer.assetThat(scopeA).hasInstanceTypes(Unlimited.class);
        observer.assetThat(scopeB).hasNoInstances();
        observer.assetThat(scopeC).hasNoInstances();
    }

    @SuppressWarnings("unchecked") private static class HashMapInstanceManager implements InstanceManager {
        private HashMap<Class, InstanceFactory> factories = new HashMap<>();

        HashMapInstanceManager() {
            factories.put(Limited.class, new LimitedFactory());
            factories.put(Unlimited.class, new UnlimitedFactory());
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

    private static class Limited {}
    private static class LimitedFactory extends InstanceFactory<Limited> {
        @Override public Limited create(Scope scope) { return new Limited(); }
        @Override public String getLimit() { return LIMIT; }
    }

    private static class Unlimited {}
    private static class UnlimitedFactory extends InstanceFactory<Unlimited> {
        @Override public Unlimited create(Scope scope) { return new Unlimited(); }
    }
}
