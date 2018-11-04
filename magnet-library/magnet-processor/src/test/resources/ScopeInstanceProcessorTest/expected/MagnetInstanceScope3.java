package test;

import magnet.internal.InstanceScope;

final class MagnetInstanceScope3 extends InstanceScope implements Scope3 {
    public MagnetInstanceScope3() {
        super(true);
    }

    @Override
    public void bind(ParentScope scope) {
        setParentScope((InstanceScope) scope);
    }
}