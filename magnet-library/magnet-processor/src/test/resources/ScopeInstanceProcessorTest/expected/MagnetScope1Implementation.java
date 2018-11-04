package test;

import java.util.List;
import magnet.Classifier;
import magnet.internal.InstanceScope;

final class MagnetScope1Implementation extends InstanceScope implements Scope1 {
    public MagnetScope1Implementation() {
        super(false);
    }

    @Override
    public String getName1() {
        return requireScopeContainer().getSingle(String.class, Classifier.NONE);
    }

    @Override
    public String getName2() {
        return requireScopeContainer().getSingle(String.class, "name2");
    }

    @Override
    public String getName3() {
        return requireScopeContainer().getOptional(String.class, Classifier.NONE);
    }

    @Override
    public String getName4() {
        return requireScopeContainer().getOptional(String.class, "name4");
    }

    @Override
    public List<String> getName5() {
        return requireScopeContainer().getMany(String.class, Classifier.NONE);
    }

    @Override
    public List<String> getName6() {
        return requireScopeContainer().getMany(String.class, "name6");
    }
}