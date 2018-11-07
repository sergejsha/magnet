package test;

import magnet.Scope;
import magnet.internal.InstanceFactory;

public final class Delegate1MagnetFactory extends InstanceFactory<Delegate1> {
    @Override
    public Delegate1 create(Scope scope) {
        return new Delegate1();
    }

    public static Class getType() {
        return Delegate1.class;
    }
}