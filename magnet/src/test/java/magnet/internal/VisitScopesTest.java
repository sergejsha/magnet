package magnet.internal;

import magnet.Magnet;
import magnet.Scope;
import magnet.internal.events.ObservableScopeVisitor;
import magnet.internal.events.OnEnterScope;
import magnet.internal.events.OnExitScope;
import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class VisitScopesTest {

    private Scope scopeA;
    private Scope scopeB;
    private Scope scopeB1;
    private Scope scopeB2;
    private Scope scopeC;

    private ObservableScopeVisitor visitor = new ObservableScopeVisitor();

    @Before
    public void before() {
        scopeA = Magnet.createRootScope();
        scopeB = scopeA.createSubscope();
        scopeB1 = scopeB.createSubscope();
        scopeB2 = scopeB.createSubscope();
        scopeC = scopeA.createSubscope();
    }

    @Test
    public void visitRootScope_DepthUnlimited() {
        scopeA.accept(visitor, Integer.MAX_VALUE);
        assertThat(visitor.visited).containsExactly(
            new OnEnterScope(scopeA, null),
            new OnEnterScope(scopeC, scopeA),
            new OnExitScope(scopeC),
            new OnEnterScope(scopeB, scopeA),
            new OnEnterScope(scopeB2, scopeB),
            new OnExitScope(scopeB2),
            new OnEnterScope(scopeB1, scopeB),
            new OnExitScope(scopeB1),
            new OnExitScope(scopeB),
            new OnExitScope(scopeA)
        ).inOrder();
    }

    @Test
    public void visitRootScope_Depth0() {
        scopeA.accept(visitor, 0);
        assertThat(visitor.visited).containsExactly(
            new OnEnterScope(scopeA, null),
            new OnExitScope(scopeA)
        ).inOrder();
    }

    @Test
    public void visitRootScope_Depth1() {
        scopeA.accept(visitor, 1);
        assertThat(visitor.visited).containsExactly(
            new OnEnterScope(scopeA, null),
            new OnEnterScope(scopeC, scopeA),
            new OnExitScope(scopeC),
            new OnEnterScope(scopeB, scopeA),
            new OnExitScope(scopeB),
            new OnExitScope(scopeA)
        ).inOrder();
    }

    @Test
    public void visitChildScope_Depth0() {
        scopeB.accept(visitor, 0);
        assertThat(visitor.visited).containsExactly(
            new OnEnterScope(scopeB, scopeA),
            new OnExitScope(scopeB)
        ).inOrder();
    }

    @Test
    public void visitChildScope_Depth1() {
        scopeB.accept(visitor, 1);
        assertThat(visitor.visited).containsExactly(
            new OnEnterScope(scopeB, scopeA),
            new OnEnterScope(scopeB2, scopeB),
            new OnExitScope(scopeB2),
            new OnEnterScope(scopeB1, scopeB),
            new OnExitScope(scopeB1),
            new OnExitScope(scopeB)
        ).inOrder();
    }
}
