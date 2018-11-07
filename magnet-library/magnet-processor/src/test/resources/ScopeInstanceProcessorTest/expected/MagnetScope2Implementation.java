package test;

import magnet.Classifier;
import magnet.ScopeContainer;

final class MagnetScope2Implementation implements Scope2 {
    private final ScopeContainer scopeContainer;
    public MagnetScope2Implementation(ScopeContainer scopeContainer) {
        this.scopeContainer = scopeContainer;
    }

    @Override
    public void bind1(String value) {
        scopeContainer.bind(String.class, value, Classifier.NONE);
    }

    @Override
    public void bind2(String value) {
        scopeContainer.bind(String.class, value, "bind2");
    }

}