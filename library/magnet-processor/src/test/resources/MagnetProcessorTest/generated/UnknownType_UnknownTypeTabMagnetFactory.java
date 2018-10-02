package app.extension;

import magnet.Scope;
import magnet.Scoping;
import magnet.internal.InstanceFactory;

public final class UnknownTypeTabMagnetFactory implements InstanceFactory<Tab> {
    @Override
    public Tab create(Scope scope) {
        return new UnknownTypeTab();
    }

    @Override
    public Scoping getScoping() {
        return Scoping.TOPMOST;
    }

    public static Class getType() {
        return Tab.class;
    }
}