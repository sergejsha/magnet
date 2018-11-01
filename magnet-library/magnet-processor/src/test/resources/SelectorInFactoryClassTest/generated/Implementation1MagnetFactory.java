package selector;

import magnet.internal.InstanceFactory;
import magnet.internal.InstanceScope;

public final class Implementation1MagnetFactory extends InstanceFactory<Interface> {
    @Override
    public Interface create(InstanceScope scope) {
        return new Implementation1();
    }

    public static Class getType() {
        return Interface.class;
    }
}