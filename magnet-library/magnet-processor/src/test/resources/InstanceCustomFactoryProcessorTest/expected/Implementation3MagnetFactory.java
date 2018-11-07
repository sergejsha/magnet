package test;

import magnet.Scope;
import magnet.internal.InstanceFactory;

public final class Implementation3MagnetFactory extends InstanceFactory<Interface3> {

    private CustomFactory3 factory = null;

    @Override
    public Interface3 create(Scope scope) {
        if (factory == null) {
            factory = new CustomFactory3();
        }
        return factory.create(scope, Interface3.class, "");
    }

    public static Class getType() {
        return Interface3.class;
    }
}
