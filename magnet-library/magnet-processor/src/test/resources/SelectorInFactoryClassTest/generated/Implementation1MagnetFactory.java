package selector;

import magnet.Scope;
import magnet.internal.Generated;
import magnet.internal.InstanceFactory;

@Generated
public final class Implementation1MagnetFactory extends InstanceFactory<Interface> {
    @Override
    public Interface create(Scope scope) {
        return new Implementation1();
    }

    public static Class getType() {
        return Interface.class;
    }
}