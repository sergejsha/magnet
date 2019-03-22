package test;

import magnet.Scope;
import magnet.internal.Generated;
import magnet.internal.InstanceFactory;

@Generated
public final class Interface1DelegateMagnetFactory extends InstanceFactory<Interface1.Delegate> {
    @Override
    public Interface1.Delegate create(Scope scope) {
        return new Interface1.Delegate();
    }

    public static Class getType() {
        return Interface1.Delegate.class;
    }
}