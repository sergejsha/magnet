package siblings;

import magnet.Scope;
import magnet.internal.InstanceFactory;

public final class Implementation4Interface1MagnetFactory extends InstanceFactory<Interface1> {
    private static Class[] SIBLING_TYPES = {Interface2.class, Implementation4Interface2MagnetFactory.class};

    @Override
    public Interface1 create(Scope scope) {
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
