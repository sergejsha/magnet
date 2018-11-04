package test;

import magnet.internal.ScopeFactory;

public final class MagnetScope1Factory extends ScopeFactory<Scope1> {
    @Override
    public Scope1 create() {
        return new MagnetScope1Implementation();
    }

    public static Class getType() {
        return Scope1.class;
    }

}