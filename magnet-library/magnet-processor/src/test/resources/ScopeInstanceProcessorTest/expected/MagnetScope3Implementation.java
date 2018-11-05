package test;

import magnet.internal.InstanceScope;

final class MagnetScope3Implementation extends InstanceScope implements Scope3 {
    public MagnetScope3Implementation() {
        super(true);
    }

    @Override
    public void bind(Scope3_1 scope) {
        setParentScope((InstanceScope) scope);
    }
}