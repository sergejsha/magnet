package test;

import magnet.ScopeContainer;
import magnet.internal.InstanceFactory;

public final class Implementation2MagnetFactory extends InstanceFactory<Interface2> {

    private CustomFactory2<Interface2> factory = null;

    @Override
    public Interface2 create(ScopeContainer scope) {
        if (factory == null) {
            factory = new CustomFactory2<Interface2>();
        }
        return factory.create(scope, Interface2.class, "");
    }

    public static Class getType() {
        return Interface2.class;
    }
}
