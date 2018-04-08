package app.extension;

import magnet.Scope;
import magnet.internal.Factory;

public final class MagnetUnknownTypeTabFactory implements Factory<Tab> {
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