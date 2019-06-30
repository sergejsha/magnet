package magnet.inspection.events;

import magnet.inspection.Instance;

import java.util.Objects;

public class OnInstance {
    private final Instance instance;

    public OnInstance(Instance instance) {
        this.instance = instance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OnInstance that = (OnInstance) o;
        return instance.equals(that.instance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instance);
    }
}
