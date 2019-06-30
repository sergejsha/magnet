package magnet.inspection.events;

import magnet.Scope;

import java.util.Objects;

public class OnExitScope {
    private final Scope scope;

    public OnExitScope(Scope scope) {
        this.scope = scope;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OnExitScope that = (OnExitScope) o;
        return scope.equals(that.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scope);
    }

    @Override
    public String toString() {
        return "OnExitScope: " + scope;
    }
}
