package magnet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of this interface should be used with {@link magnet.Scope#accept(Visitor, int)}
 * for iterating though instances and subscopes of a scope. Scope visiting begins with iterating
 * through all instances and then through all subscopes.
 */
public interface Visitor {

    /** Provision type. */
    enum Provision {BOUND, INJECTED}

    /** Visited instance. */
    interface Instance {
        @NotNull Scoping getScoping();
        @NotNull String getClassifier();
        @NotNull String getLimit();
        @NotNull Class<?> getType();
        @NotNull Object getValue();
        @NotNull Provision getProvision();
    }

    /** Visited scope. */
    interface Scope {
        @Nullable String[] getLimits();
    }

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
