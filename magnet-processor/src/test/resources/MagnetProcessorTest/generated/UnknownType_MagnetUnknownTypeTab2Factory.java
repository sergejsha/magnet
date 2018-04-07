package app.extension;

import magnet.Scope;
import magnet.internal.Factory;

public final class MagnetUnknownTypeTab2Factory implements Factory<Tab> {
    @Override
    public Tab create(Scope scope) {
        return new UnknownTypeTab2();
    }

    public static Class getType() {
        return Tab.class;
    }
}