package test;

import magnet.Scope;
import magnet.internal.ScopeFactory;

public final class MagnetScope1Factory extends ScopeFactory<Scope1> {
    @Override
    public Scope1 create(Scope scopeContainer) {
        return new MagnetScope1Implementation(scopeContainer);
    }

    public static Class getType() {
        return Scope1.class;
    }

}