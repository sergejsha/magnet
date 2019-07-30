package magnet.internal.events;

import magnet.Scoping;
import magnet.Visitor.Instance;
import magnet.Visitor.Provision;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class OnInstance implements Comparable<OnInstance> {
    private final Provision provision;
    private final Class<?> type;
    private final Object object;
    private final String classifier;
    private final Scoping scoping;

    OnInstance(Instance object) {
        this.provision = object.getProvision();
        this.type = object.getType();
        this.object = object.getValue();
        this.classifier = object.getClassifier();
        this.scoping = object.getScoping();
    }

    public OnInstance(
        Provision provision,
        Class<?> type,
        Object object,
        String classifier,
        Scoping scoping
    ) {
        this.provision = provision;
        this.type = type;
        this.object = object;
        this.classifier = classifier;
        this.scoping = scoping;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OnInstance that = (OnInstance) o;
        return provision == that.provision &&
            type.equals(that.type) &&
            object.equals(that.object) &&
            classifier.equals(that.classifier) &&
            scoping == that.scoping;
    }

    @Override public int hashCode() {
        return Objects.hash(provision, type, object, classifier, scoping);
    }

    @Override public String toString() {
        return "OnInstance{ "
            + provision + " "
            + scoping + " "
            + type.getSimpleName() + "@" + (classifier.length() == 0 ? "NONE" : classifier) + " }";
    }

    @Override public int compareTo(@NotNull OnInstance o) {
        return provision.compareTo(o.provision) * 100
            + type.getName().compareTo(o.type.getName()) * 10
            + classifier.compareTo(o.classifier);
    }
}
