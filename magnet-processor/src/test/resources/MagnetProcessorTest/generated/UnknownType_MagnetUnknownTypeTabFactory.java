package app.extension;

import magnet.InstanceFactory;
import magnet.InstanceRetention;
import magnet.Scope;

public final class MagnetUnknownTypeTabFactory implements InstanceFactory<Tab> {
    @Override
    public Tab create(Scope scope) {
        return new UnknownTypeTab();
    }

    @Override
    public InstanceRetention getInstanceRetention() {
        return InstanceRetention.SCOPE;
    }

    public static Class getType() {
        return Tab.class;
    }
}