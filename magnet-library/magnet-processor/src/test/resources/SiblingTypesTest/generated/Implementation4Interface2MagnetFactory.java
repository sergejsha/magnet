package siblings;

import magnet.Scope;
import magnet.Scoping;
import magnet.internal.InstanceFactory;

public final class Implementation4Interface2MagnetFactory extends InstanceFactory<Interface2> {
    private static Class[] SIBLING_TYPES = {Interface1.class, Implementation4Interface1MagnetFactory.class};

    @Override
    public Interface2 create(Scope scope) {
        return new Implementation4();
    }

    @Override
    public Scoping getScoping() {
        return Scoping.TOPMOST;
    }

    @Override
    public Class[] getSiblingTypes() {
        return SIBLING_TYPES;
    }

    public static Class getType() {
        return Interface2.class;
    }
}
