package selector;

import magnet.internal.InstanceFactory;
import magnet.internal.ScopeContainer;

public final class Implementation1MagnetFactory extends InstanceFactory<Interface> {
    @Override
    public Interface create(ScopeContainer scope) {
        return new Implementation1();
    }

    public static Class getType() {
        return Interface.class;
    }
}