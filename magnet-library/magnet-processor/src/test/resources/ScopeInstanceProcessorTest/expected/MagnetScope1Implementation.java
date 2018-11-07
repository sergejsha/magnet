package test;

import java.util.List;
import magnet.Classifier;
import magnet.Scope;

final class MagnetScope1Implementation implements Scope1 {
    private final Scope scopeContainer;
    public MagnetScope1Implementation(Scope scopeContainer) {
        this.scopeContainer = scopeContainer;
    }

    @Override
    public String getName1() {
        return scopeContainer.getSingle(String.class, Classifier.NONE);
    }

    @Override
    public String getName2() {
        return scopeContainer.getSingle(String.class, "name2");
    }

    @Override
    public String getName3() {
        return scopeContainer.getOptional(String.class, Classifier.NONE);
    }

    @Override
    public String getName4() {
        return scopeContainer.getOptional(String.class, "name4");
    }

    @Override
    public List<String> getName5() {
        return scopeContainer.getMany(String.class, Classifier.NONE);
    }

    @Override
    public List<String> getName6() {
        return scopeContainer.getMany(String.class, "name6");
    }
}