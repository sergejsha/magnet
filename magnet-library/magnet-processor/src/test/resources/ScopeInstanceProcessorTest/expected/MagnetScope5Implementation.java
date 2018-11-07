package test;

import magnet.Classifier;
import magnet.ScopeContainer;

final class MagnetScope5Implementation implements Scope5 {
    private final ScopeContainer scopeContainer;
    public MagnetScope5Implementation(ScopeContainer scopeContainer) {
        this.scopeContainer = scopeContainer;
    }

    @Override
    public String get0() {
        return scopeContainer.getSingle(String.class, Classifier.NONE);
    }

    @Override
    public String get1() {
        return scopeContainer.getSingle(String.class, Classifier.NONE);
    }

    @Override
    public String get2() {
        return scopeContainer.getSingle(String.class, Classifier.NONE);
    }

    @Override
    public String get3() {
        return scopeContainer.getSingle(String.class, Classifier.NONE);
    }

}