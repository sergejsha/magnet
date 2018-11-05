package test;

import magnet.Classifier;
import magnet.internal.InstanceScope;

final class MagnetScope2Implementation extends InstanceScope implements Scope2 {
    public MagnetScope2Implementation() {
        super(false);
    }

    @Override
    public void bind1(String value) {
        requireScopeContainer().bind(String.class, value, Classifier.NONE);
    }

    @Override
    public void bind2(String value) {
        requireScopeContainer().bind(String.class, value, "bind2");
    }

}