package test;

import magnet.ScopeContainer;
import magnet.internal.InstanceFactory;

public final class Delegate1MagnetFactory extends InstanceFactory<Delegate1> {
    @Override
    public Delegate1 create(ScopeContainer scope) {
        return new Delegate1();
    }

    public static Class getType() {
        return Delegate1.class;
    }
}