package test;

import magnet.Classifier;
import magnet.internal.InstanceScope;

final class MagnetScope5Implementation extends InstanceScope implements Scope5 {
    public MagnetScope5Implementation() {
        super(false);
    }

    @Override
    public String get0() {
        return requireScopeContainer().getSingle(String.class, Classifier.NONE);
    }

    @Override
    public String get1() {
        return requireScopeContainer().getSingle(String.class, Classifier.NONE);
    }

    @Override
    public String get2() {
        return requireScopeContainer().getSingle(String.class, Classifier.NONE);
    }

    @Override
    public String get3() {
        return requireScopeContainer().getSingle(String.class, Classifier.NONE);
    }

}