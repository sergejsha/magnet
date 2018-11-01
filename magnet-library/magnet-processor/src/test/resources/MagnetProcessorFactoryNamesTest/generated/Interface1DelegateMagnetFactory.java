package test;

import magnet.internal.InstanceFactory;
import magnet.internal.ScopeContainer;

public final class Interface1DelegateMagnetFactory extends InstanceFactory<Interface1.Delegate> {
    @Override
    public Interface1.Delegate create(ScopeContainer scope) {
        return new Interface1.Delegate();
    }

    public static Class getType() {
        return Interface1.Delegate.class;
    }
}