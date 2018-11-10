package test;

import magnet.Factory;
import magnet.Scope;
import magnet.Scoping;
import magnet.internal.InstanceFactory;

public final class Implementation1MagnetFactory extends InstanceFactory<Interface1> implements Factory.Instantiator<Interface1> {

    private CustomFactory1 factory = null;

    @Override
    public Interface1 create(Scope scope) {
        if (factory == null) {
            factory = new CustomFactory1();
        }
        return factory.create(scope, Interface1.class, "", Scoping.TOPMOST, this);
    }

    @Override
    public Interface1 instantiate(Scope scope) {
        return new Implementation1();
    }

    public static Class getType() {
        return Interface1.class;
    }
}
