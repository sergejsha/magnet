package test;

import magnet.internal.InstanceScope;

final class MagnetScope3Implementation extends InstanceScope implements Scope3 {
    public MagnetScope3Implementation() {
        super(true);
    }

    @Override
    public void bind(ParentScope scope) {
        setParentScope((InstanceScope) scope);
    }
}