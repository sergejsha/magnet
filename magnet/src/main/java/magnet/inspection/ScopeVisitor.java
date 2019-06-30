package magnet.inspection;

import magnet.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ScopeVisitor {

    /**
     * Called when new scope is entered.
     *
     * @param scope  entered scope.
     * @param parent parent scope of the entered scope.
     * @return <code>true</code> to visit instances of this scope, <code>false</code> to skip instances.
     */
    boolean onEnterScope(@NotNull Scope scope, @Nullable Scope parent);

    /**
     * Called when visiting new instance between {@link #onEnterScope(Scope, Scope)}
     * and {@link #onExitScope(Scope)} calls.
     *
     * @param instance visited instance.
     * @return <code>true</code> to visit the next instance in the scope or <code>false</code>
     * to skip all other instances in this scope.
     */
    boolean onInstance(@NotNull Instance instance);

    /**
     * Called when previously entered scope is exited.
     *
     * @param scope exited scope.
     */
    void onExitScope(@NotNull Scope scope);
}
