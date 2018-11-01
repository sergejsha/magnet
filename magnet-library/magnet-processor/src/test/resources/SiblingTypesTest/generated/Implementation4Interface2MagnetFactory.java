package siblings;

import magnet.internal.InstanceFactory;
import magnet.internal.ScopeContainer;

public final class Implementation4Interface2MagnetFactory extends InstanceFactory<Interface2> {
    private static Class[] SIBLING_TYPES = {Interface1.class, Implementation4Interface1MagnetFactory.class};

    @Override
    public Interface2 create(ScopeContainer scope) {
        return new Implementation4();
    }

    @Override
    public Class[] getSiblingTypes() {
        return SIBLING_TYPES;
    }

    public static Class getType() {
        return Interface2.class;
    }
}
