package test;

import magnet.ScopeContainer;
import magnet.internal.InstanceFactory;

public final class Implementation1MagnetFactory extends InstanceFactory<Interface1> {

    private CustomFactory1 factory = null;

    @Override
    public Interface1 create(ScopeContainer scope) {
        if (factory == null) {
            factory = new CustomFactory1();
        }
        return factory.create(scope, Interface1.class, "");
    }

    public static Class getType() {
        return Interface1.class;
    }
}
