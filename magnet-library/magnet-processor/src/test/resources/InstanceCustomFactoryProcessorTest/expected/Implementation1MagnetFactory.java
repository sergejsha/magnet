package test;

import magnet.Scope;
import magnet.internal.InstanceFactory;

public final class Implementation1MagnetFactory extends InstanceFactory<Interface1> {

    private CustomFactory1 factory = null;

    @Override
    public Interface1 create(Scope scope) {
        if (factory == null) {
            factory = new CustomFactory1();
        }
        return factory.create(scope, Interface1.class, "");
    }

    public static Class getType() {
        return Interface1.class;
    }
}
