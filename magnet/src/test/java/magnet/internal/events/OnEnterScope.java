package magnet.internal.events;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class OnEnterScope {
    private final @NotNull Object scope;
    private final @Nullable Object parent;

    public OnEnterScope(@NotNull Object scope, @Nullable Object parent) {
        this.scope = scope;
        this.parent = parent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OnEnterScope that = (OnEnterScope) o;
        return scope.equals(that.scope) &&
            Objects.equals(parent, that.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scope, parent);
    }

    @Override
    public String toString() {
        return "OnEnterScope: " + scope;
    }
}
