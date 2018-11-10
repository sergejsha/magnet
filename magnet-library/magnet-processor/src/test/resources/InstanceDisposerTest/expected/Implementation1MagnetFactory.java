package test;

import magnet.Scope;
import magnet.internal.InstanceFactory;

public final class Implementation1MagnetFactory extends InstanceFactory<Interface> {

    @Override
    public Interface create(Scope scope) {
        return new Implementation1();
    }

    @Override
    public boolean isDisposable() {
        return true;
    }

    @Override
    public void dispose(Interface instance) {
        ((Implementation1) instance).disposeIt();
    }

    public static Class getType() {
        return Interface.class;
    }
}
