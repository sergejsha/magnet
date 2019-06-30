package magnet.inspection.events;

import magnet.Scope;
import magnet.inspection.Instance;
import magnet.inspection.ScopeVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ObservableScopeVisitor implements ScopeVisitor {

    public List<Object> visited = new ArrayList<>();

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
