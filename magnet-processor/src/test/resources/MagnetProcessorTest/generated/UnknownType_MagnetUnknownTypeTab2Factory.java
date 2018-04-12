package app.extension;

import magnet.InstanceFactory;
import magnet.InstanceRetention;
import magnet.Scope;

public final class MagnetUnknownTypeTab2Factory implements InstanceFactory<Tab> {
    @Override
    public Tab create(Scope scope) {
        return new UnknownTypeTab2();
    }

    @Override
    public InstanceRetention getInstanceRetention() {
        return InstanceRetention.SCOPE;
    }

    public static Class getType() {
        return Tab.class;
    }
}