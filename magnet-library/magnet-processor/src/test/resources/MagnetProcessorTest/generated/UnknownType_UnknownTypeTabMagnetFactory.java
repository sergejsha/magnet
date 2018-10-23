package app.extension;

import magnet.Scope;
import magnet.internal.InstanceFactory;

public final class UnknownTypeTabMagnetFactory extends InstanceFactory<Tab> {
    @Override
    public Tab create(Scope scope) {
        return new UnknownTypeTab();
    }

    public static Class getType() {
        return Tab.class;
    }
}