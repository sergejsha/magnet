package test;

import magnet.internal.ScopeFactory;

public final class Scope1MagnetFactory extends ScopeFactory<Scope1> {
    @Override
    public Scope1 create() {
        return new MagnetInstanceScope1();
    }
}