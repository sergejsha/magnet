package test;

import magnet.Scope;
import magnet.internal.InstanceFactory;

public final class Implementation2MagnetFactory extends InstanceFactory<Interface2> {

    private CustomFactory2<Interface2> factory = null;

    @Override
    public Interface2 create(Scope scope) {
        if (factory == null) {
            factory = new CustomFactory2<Interface2>();
        }
        return factory.create(scope, Interface2.class, "");
    }

    public static Class getType() {
        return Interface2.class;
    }
}
