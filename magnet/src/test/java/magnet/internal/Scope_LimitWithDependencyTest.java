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
public class Scope_LimitWithDependencyTest {

    private final static String LIMIT_ONE = "limit-one";
    private final static String LIMIT_TWO = "limit-two";

    @Mock private Scope scopeA;
    @Mock private Scope scopeB;
    @Mock private Scope scopeC;
    @Mock private Scope scopeD;

    @Test
    public void test_instanceMovesDown_ifDependencyLimitLiesBelow() {
        // given
        scopeA = InternalFactory.createRootScope(new MapInstanceManager());
        scopeB = scopeA.createSubscope().limit(LIMIT_ONE);
        scopeC = scopeB.createSubscope();
        scopeD = scopeC.createSubscope().limit(LIMIT_TWO);

        // when
        scopeD.getSingle(LimitedOne.class);

        // then
        ScopeObserver observer = new ScopeObserver();
        scopeA.accept(observer, Integer.MAX_VALUE);
        observer.assetThat(scopeA).hasNoInstances();
        observer.assetThat(scopeB).hasNoInstances();
        observer.assetThat(scopeC).hasNoInstances();
        observer.assetThat(scopeD).hasInstanceTypes(LimitedOne.class, LimitedTwo.class);
    }

    @Test
    public void test_instancesLay_whereThereLimitsLies() {
        // given
        scopeA = InternalFactory.createRootScope(new MapInstanceManager());
        scopeB = scopeA.createSubscope().limit(LIMIT_TWO);
        scopeC = scopeB.createSubscope();
        scopeD = scopeC.createSubscope().limit(LIMIT_ONE);

        // when
        scopeD.getSingle(LimitedOne.class);

        // then
        ScopeObserver observer = new ScopeObserver();
        scopeA.accept(observer, Integer.MAX_VALUE);
        observer.assetThat(scopeA).hasNoInstances();
        observer.assetThat(scopeB).hasInstanceTypes(LimitedTwo.class);
        observer.assetThat(scopeC).hasNoInstances();
        observer.assetThat(scopeD).hasInstanceTypes(LimitedOne.class);
    }

    @Test
    public void test_instancesCollocated_ifDependencyLimitsCollocated() {
        // given
        scopeA = InternalFactory.createRootScope(new MapInstanceManager());
        scopeB = scopeA.createSubscope().limit(LIMIT_ONE, LIMIT_TWO);
        scopeC = scopeB.createSubscope();

        // when
        scopeC.getSingle(LimitedOne.class);

        // then
        ScopeObserver observer = new ScopeObserver();
        scopeA.accept(observer, Integer.MAX_VALUE);
        observer.assetThat(scopeA).hasNoInstances();
        observer.assetThat(scopeB).hasInstanceTypes(LimitedOne.class, LimitedTwo.class);
        observer.assetThat(scopeC).hasNoInstances();
    }

    @SuppressWarnings("unchecked") private static class MapInstanceManager implements InstanceManager {
        private HashMap<Class, InstanceFactory> factories = new HashMap<>();

        MapInstanceManager() {
            factories.put(LimitedOne.class, new LimitedOneFactory());
            factories.put(LimitedTwo.class, new LimitedTwoFactory());
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

    private static class LimitedOne {}
    private static class LimitedOneFactory extends InstanceFactory<LimitedOne> {
        @Override public LimitedOne create(Scope scope) {
            scope.getSingle(LimitedTwo.class);
            return new LimitedOne();
        }
        @Override public String getLimit() { return LIMIT_ONE; }
    }

    private static class LimitedTwo {}
    private static class LimitedTwoFactory extends InstanceFactory<LimitedTwo> {
        @Override public LimitedTwo create(Scope scope) { return new LimitedTwo(); }
        @Override public String getLimit() { return LIMIT_TWO; }
    }
}
