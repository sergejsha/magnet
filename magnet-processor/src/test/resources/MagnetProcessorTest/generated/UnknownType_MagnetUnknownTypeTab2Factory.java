package app.extension;

import magnet.DependencyScope;
import magnet.internal.Factory;

public final class MagnetUnknownTypeTab2Factory implements Factory<Tab> {
    @Override
    public Tab create(DependencyScope dependencyScope) {
        return new UnknownTypeTab2();
    }

    public static Class getType() {
        return Tab.class;
    }
}