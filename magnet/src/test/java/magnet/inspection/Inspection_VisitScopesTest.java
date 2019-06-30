package magnet.inspection;

import magnet.Magnet;
import magnet.Scope;
import magnet.inspection.events.OnEnterScope;
import magnet.inspection.events.OnExitScope;
import magnet.inspection.events.OnInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class Inspection_VisitScopesTest {

    private Scope scopeA;
    private Scope scopeB;
    private Scope scopeB1;
    private Scope scopeB2;
    private Scope scopeC;

    private ObservableScopeVisitor visitor;

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
        visitor = new ObservableScopeVisitor();
        scopeA.accept(visitor, Integer.MAX_VALUE);
        assertThat(visitor.visited).containsExactly(
                new OnEnterScope(scopeA, null),
                new OnEnterScope(scopeB, scopeA),
                new OnExitScope(scopeB),
                new OnEnterScope(scopeC, scopeA),
                new OnExitScope(scopeC),
                new OnEnterScope(scopeB1, scopeB),
                new OnExitScope(scopeB1),
                new OnEnterScope(scopeB2, scopeB),
                new OnExitScope(scopeB2),
                new OnExitScope(scopeA)
        );
    }

    @Test
    public void visitRootScope_Depth0() {
        visitor = new ObservableScopeVisitor();
        scopeA.accept(visitor, 0);
        assertThat(visitor.visited).containsExactly(
                new OnEnterScope(scopeA, null),
                new OnExitScope(scopeA)
        );
    }

    @Test
    public void visitRootScope_Depth1() {
        visitor = new ObservableScopeVisitor();
        scopeA.accept(visitor, 1);
        assertThat(visitor.visited).containsExactly(
                new OnEnterScope(scopeA, null),
                new OnEnterScope(scopeB, scopeA),
                new OnExitScope(scopeB),
                new OnEnterScope(scopeC, scopeA),
                new OnExitScope(scopeC),
                new OnExitScope(scopeA)
        );
    }

    @Test
    public void visitChildScope_Depth0() {
        visitor = new ObservableScopeVisitor();
        scopeB.accept(visitor, 0);
        assertThat(visitor.visited).containsExactly(
                new OnEnterScope(scopeB, scopeA),
                new OnExitScope(scopeB)
        );
    }

    @Test
    public void visitChildScope_Depth1() {
        visitor = new ObservableScopeVisitor();
        scopeB.accept(visitor, 1);
        assertThat(visitor.visited).containsExactly(
                new OnEnterScope(scopeB, scopeA),
                new OnEnterScope(scopeB1, scopeB),
                new OnExitScope(scopeB1),
                new OnEnterScope(scopeB2, scopeB),
                new OnExitScope(scopeB2),
                new OnExitScope(scopeB)
        );
    }

    static class ObservableScopeVisitor implements ScopeVisitor {

        List<Object> visited = new ArrayList<>();

        @Override
        public boolean onEnterScope(@NotNull Scope scope, @Nullable Scope parent) {
            visited.add(new OnEnterScope(scope, parent));
            return true;
        }

        @Override
        public boolean onInstance(@NotNull Instance instance) {
            visited.add(new OnInstance(instance));
            return true;
        }

        @Override
        public void onExitScope(@NotNull Scope scope) {
            visited.add(new OnExitScope(scope));
        }
    }
}
