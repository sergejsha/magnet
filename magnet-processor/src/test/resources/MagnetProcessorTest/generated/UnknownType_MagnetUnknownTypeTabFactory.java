package app.extension;

import magnet.Factory;
import magnet.Scope;

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