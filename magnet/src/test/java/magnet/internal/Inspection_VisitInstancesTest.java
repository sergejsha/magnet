package magnet.internal;

import magnet.Classifier;
import magnet.Magnet;
import magnet.Scope;
import magnet.Scoping;
import magnet.internal.events.ObservableScopeVisitor;
import magnet.internal.events.OnEnterScope;
import magnet.internal.events.OnExitScope;
import magnet.internal.events.OnInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static magnet.Scope.Visitor.Instance.Provision.BOUND;
import static magnet.Scope.Visitor.Instance.Provision.INJECTED;

public class Inspection_VisitInstancesTest {

    private InstrumentedScope scopeA;
    private InstrumentedScope scopeB;

    private Bound bound = new Bound();
    private Bound boundA = new Bound();
    private Bound boundB = new Bound();
    private String classifierA = "classifierA";
    private String classifierB = "classifierB";

    private InjectedTopMost injectedTopMost = new InjectedTopMost();
    private InjectedTopMostFactory injectedTopMostFactory = new InjectedTopMostFactory();

    private InjectedDirect injectedDirect = new InjectedDirect();
    private InjectedDirectFactory injectedDirectFactory = new InjectedDirectFactory();

    private ObservableScopeVisitor visitor;

    @Before
    public void before() {
        scopeA = (InstrumentedScope) new InstrumentedScope(Magnet.createRootScope())
            .instrumentObjectIntoScope(injectedTopMostFactory, InjectedTopMost.class, injectedTopMost, Classifier.NONE)
            .instrumentObjectIntoScope(injectedTopMostFactory, InjectedTopMost.class, injectedTopMost, classifierA)
            .instrumentObjectIntoScope(injectedTopMostFactory, InjectedTopMost.class, injectedTopMost, classifierB)
            .bind(Bound.class, bound)
            .bind(Bound.class, boundA, classifierA);

        scopeB = (InstrumentedScope) ((InstrumentedScope) scopeA.createSubscope())
            .instrumentObjectIntoScope(injectedDirectFactory, InjectedDirect.class, injectedDirect, Classifier.NONE)
            .bind(Bound.class, boundB, classifierB);
    }

    @Test
    public void visitAllScopes() {
        visitor = new ObservableScopeVisitor();
        scopeA.accept(visitor, Integer.MAX_VALUE);

        assertThat(visitor.visited).containsExactly(
            new OnEnterScope(scopeA.scope, null),
            new OnInstance(BOUND, Bound.class, bound, Classifier.NONE, Scoping.DIRECT),
            new OnInstance(BOUND, Bound.class, boundA, classifierA, Scoping.DIRECT),
            new OnInstance(INJECTED, InjectedTopMost.class, injectedTopMost, Classifier.NONE, Scoping.TOPMOST),
            new OnInstance(INJECTED, InjectedTopMost.class, injectedTopMost, classifierA, Scoping.TOPMOST),
            new OnInstance(INJECTED, InjectedTopMost.class, injectedTopMost, classifierB, Scoping.TOPMOST),
            new OnEnterScope(scopeB.scope, scopeA.scope),
            new OnInstance(BOUND, Bound.class, boundB, classifierB, Scoping.DIRECT),
            new OnInstance(INJECTED, InjectedDirect.class, injectedDirect, Classifier.NONE, Scoping.DIRECT),
            new OnExitScope(scopeB.scope),
            new OnExitScope(scopeA.scope)
        ).inOrder();
    }

    @Test
    public void visitRootScope() {
        visitor = new ObservableScopeVisitor();
        scopeA.accept(visitor, 0);
        assertThat(visitor.visited).containsExactly(
            new OnEnterScope(scopeA.scope, null),
            new OnInstance(BOUND, Bound.class, bound, Classifier.NONE, Scoping.DIRECT),
            new OnInstance(BOUND, Bound.class, boundA, classifierA, Scoping.DIRECT),
            new OnInstance(INJECTED, InjectedTopMost.class, injectedTopMost, Classifier.NONE, Scoping.TOPMOST),
            new OnInstance(INJECTED, InjectedTopMost.class, injectedTopMost, classifierA, Scoping.TOPMOST),
            new OnInstance(INJECTED, InjectedTopMost.class, injectedTopMost, classifierB, Scoping.TOPMOST),
            new OnExitScope(scopeA.scope)
        );
    }

    @Test
    public void visitScope_onEnterScope_skipAllInstances() {
        visitor = new ObservableScopeVisitor() {
            @Override
            public boolean onEnterScope(@NotNull Scope scope, @Nullable Scope parent) {
                super.onEnterScope(scope, parent);
                return false;
            }
        };
        scopeA.accept(visitor, 0);

        assertThat(visitor.visited).containsExactly(
            new OnEnterScope(scopeA.scope, null),
            new OnExitScope(scopeA.scope)
        ).inOrder();
    }

    @Test
    public void visitScope_onInstance_skipAfterSecondInstance() {
        visitor = new ObservableScopeVisitor() {
            private int count = 0;

            @Override
            public boolean onInstance(@NotNull Instance instance) {
                super.onInstance(instance);
                return ++count < 2;
            }
        };
        scopeA.accept(visitor, Integer.MAX_VALUE);

        assertThat(visitor.visited).containsExactly(
            new OnEnterScope(scopeA.scope, null),
            new OnInstance(BOUND, Bound.class, bound, Classifier.NONE, Scoping.DIRECT),
            new OnInstance(INJECTED, InjectedTopMost.class, injectedTopMost, classifierA, Scoping.TOPMOST),
            new OnEnterScope(scopeB.scope, scopeA.scope),
            new OnInstance(INJECTED, InjectedDirect.class, injectedDirect, Classifier.NONE, Scoping.DIRECT),
            new OnExitScope(scopeB.scope),
            new OnExitScope(scopeA.scope)
        ).inOrder();
    }

    private static class Bound {}

    private static class InjectedTopMost {}

    private static class InjectedDirect {}

    private class InjectedTopMostFactory extends InstanceFactory<InjectedTopMost> {
        @Override public InjectedTopMost create(Scope scope) {
            return injectedTopMost;
        }
    }

    private class InjectedDirectFactory extends InstanceFactory<InjectedDirect> {
        @Override public InjectedDirect create(Scope scope) {
            return injectedDirect;
        }

        @Override public Scoping getScoping() {
            return Scoping.DIRECT;
        }
    }
}
