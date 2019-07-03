package magnet.internal.events;

import magnet.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObservableScopeVisitor implements Scope.Visitor {

    public List<Object> visited = new ArrayList<>();
    private List<OnInstance> instances = new ArrayList<>();

    @Override
    public boolean onEnterScope(@NotNull Scope scope, @Nullable Scope parent) {
        flushInstances();
        visited.add(new OnEnterScope(scope, parent));
        return true;
    }

    @Override
    public boolean onInstance(@NotNull Instance instance) {
        instances.add(new OnInstance(instance));
        return true;
    }

    @Override
    public void onExitScope(@NotNull Scope scope) {
        flushInstances();
        visited.add(new OnExitScope(scope));
    }

    private void flushInstances() {
        if (instances.size() > 0) {
            Collections.sort(instances);
            visited.addAll(instances);
            instances.clear();
        }
    }
}
