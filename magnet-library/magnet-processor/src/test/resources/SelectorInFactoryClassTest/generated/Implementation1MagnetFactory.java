package selector;

import magnet.ScopeContainer;
import magnet.internal.InstanceFactory;

public final class Implementation1MagnetFactory extends InstanceFactory<Interface> {
    @Override
    public Interface create(ScopeContainer scope) {
        return new Implementation1();
    }

    public static Class getType() {
        return Interface.class;
    }
}