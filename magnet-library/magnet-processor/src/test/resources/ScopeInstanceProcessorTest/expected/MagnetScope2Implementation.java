package test;

import magnet.Classifier;
import magnet.Scope;

final class MagnetScope2Implementation implements Scope2 {
    private final Scope scopeContainer;
    public MagnetScope2Implementation(Scope scopeContainer) {
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