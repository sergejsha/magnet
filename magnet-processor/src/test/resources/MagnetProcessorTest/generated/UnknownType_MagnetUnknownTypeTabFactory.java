package app.extension;

import magnet.InstanceFactory;
import magnet.Scope;

public final class MagnetUnknownTypeTabFactory implements InstanceFactory<Tab> {
    @Override
    public Tab create(Scope scope) {
        return new UnknownTypeTab();
    }

    @Override
    public boolean isScoped() {
        return true;
    }

    public static Class getType() {
        return Tab.class;
    }
}