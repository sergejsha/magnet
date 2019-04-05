package app;

import magnet.Scope;
import magnet.internal.Generated;
import magnet.internal.InstanceFactory;

@Generated
public final class UnderTestProvideTypeMagnetFactory extends InstanceFactory<Type> {
    @Override
    public Type create(Scope scope) {
        return UnderTest.provideType();
    }

    public static Class getType() {
        return Type.class;
    }
}