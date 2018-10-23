package app.extension;

import magnet.Scope;
import magnet.internal.InstanceFactory;

public final class UnknownTypeTab2MagnetFactory extends InstanceFactory<Tab> {
    @Override
    public Tab create(Scope scope) {
        return new UnknownTypeTab2();
    }

    public static Class getType() {
        return Tab.class;
    }
}