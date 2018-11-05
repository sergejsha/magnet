package test;

import magnet.internal.InstanceScope;

final class MagnetScope7Implementation extends InstanceScope implements Scope7 {
    public MagnetScope7Implementation() {
        super(true);
    }

    @Override
    public void bind(Scope7_1 scope) {
        setParentScope((InstanceScope) scope);
    }
}