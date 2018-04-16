package app.extension;

import magnet.InstanceFactory;
import magnet.Scope;
import magnet.Scoping;

public final class MagnetUnknownTypeTabFactory implements InstanceFactory<Tab> {
    @Override
    public Tab create(Scope scope) {
        return new UnknownTypeTab();
    }

    @Override
    public Scoping getScoping() {
        return Scoping.SCOPE;
    }

    public static Class getType() {
        return Tab.class;
    }
}