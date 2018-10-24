package selector;

import magnet.Scope;
import magnet.internal.InstanceFactory;

public final class Implementation1MagnetFactory extends InstanceFactory<Interface> {
    @Override
    public Interface create(Scope scope) {
        return new Implementation1();
    }

    public static Class getType() {
        return Interface.class;
    }
}