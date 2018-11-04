package test;

import magnet.Classifier;
import magnet.internal.InstanceScope;

final class MagnetInstanceScope2 extends InstanceScope implements Scope2 {
    public MagnetInstanceScope2() {
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