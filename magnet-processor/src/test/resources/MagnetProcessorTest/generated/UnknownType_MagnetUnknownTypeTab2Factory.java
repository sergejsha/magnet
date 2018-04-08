package app.extension;

import magnet.InstanceFactory;
import magnet.Scope;

public final class MagnetUnknownTypeTab2Factory implements InstanceFactory<Tab> {
    @Override
    public Tab create(Scope scope) {
        return new UnknownTypeTab2();
    }

    @Override
    public boolean isScoped() {
        return true;
    }

    public static Class getType() {
        return Tab.class;
    }
}