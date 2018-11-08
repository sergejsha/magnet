package test;

import magnet.Factory;
import magnet.Scope;
import magnet.internal.InstanceFactory;

public final class Implementation3MagnetFactory extends InstanceFactory<Interface3> implements Factory.Instantiator<Interface3> {

    private CustomFactory3 factory = null;

    @Override
    public Interface3 create(Scope scope) {
        if (factory == null) {
            factory = new CustomFactory3();
        }
        return factory.create(scope, Interface3.class, "", this);
    }

    @Override
    public Interface3 instantiate(Scope scope) {
        String value1 = scope.getSingle(String.class);
        Long value2 = scope.getSingle(Long.class);
        return new Implementation3(value1, value2);
    }

    public static Class getType() {
        return Interface3.class;
    }
}
