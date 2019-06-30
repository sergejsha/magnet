package magnet.inspection.events;

import magnet.Scoping;
import magnet.inspection.Instance;

import java.util.Objects;

public class OnInstance {
    private final Instance.Provision provision;
    private final Class<?> type;
    private final Object object;
    private final String classifier;
    private final Scoping scoping;

    public OnInstance(Instance instance) {
        this.provision = instance.getProvision();
        this.type = instance.getType();
        this.object = instance.getObject();
        this.classifier = instance.getClassifier();
        this.scoping = instance.getScoping();
    }

    public OnInstance(
        Instance.Provision provision,
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OnInstance that = (OnInstance) o;
        return provision == that.provision &&
                type.equals(that.type) &&
                object.equals(that.object) &&
                classifier.equals(that.classifier) &&
                scoping == that.scoping;
    }

    @Override
    public int hashCode() {
        return Objects.hash(provision, type, object, classifier, scoping);
    }

    @Override
    public String toString() {
        return "OnInstance{" +
                "provision=" + provision +
                ", type=" + type +
                ", object=" + object +
                ", classifier='" + classifier + '\'' +
                ", scoping=" + scoping +
                '}';
    }
}
