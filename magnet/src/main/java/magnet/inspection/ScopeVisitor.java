package magnet.inspection;

import magnet.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ScopeVisitor {

    int VISIT_INSTANCES = 1;
    int VISIT_SUBSCOPES = 2;
    int VISIT_ALL = 4;

    int onEnterScope(@NotNull Scope scope, @Nullable Scope parent);
    boolean onInstance(@NotNull Instance instance);
    boolean onExitScope(@NotNull Scope scope);
}