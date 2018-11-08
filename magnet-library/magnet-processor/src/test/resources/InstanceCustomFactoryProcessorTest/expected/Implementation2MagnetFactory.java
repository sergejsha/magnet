package test;

import magnet.Factory;
import magnet.Scope;
import magnet.internal.InstanceFactory;

public final class Implementation2MagnetFactory extends InstanceFactory<Interface2> implements Factory.Instantiator<Interface2> {

    private CustomFactory2<Interface2> factory = null;

    @Override
    public Interface2 create(Scope scope) {
        if (factory == null) {
            factory = new CustomFactory2<Interface2>();
        }
        return factory.create(scope, Interface2.class, "", this);
    }

    @Override
    public Interface2 instantiate(Scope scope) {
        return new Implementation2();
    }

    public static Class getType() {
        return Interface2.class;
    }
}
