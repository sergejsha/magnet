package siblings;

import magnet.internal.InstanceFactory;
import magnet.internal.InstanceScope;

public final class Implementation4Interface1MagnetFactory extends InstanceFactory<Interface1> {
    private static Class[] SIBLING_TYPES = {Interface2.class, Implementation4Interface2MagnetFactory.class};

    @Override
    public Interface1 create(InstanceScope scope) {
        return new Implementation4();
    }

    @Override
    public Class[] getSiblingTypes() {
        return SIBLING_TYPES
    }

    public static Class getType() {
        return Interface1.class;
    }
}
