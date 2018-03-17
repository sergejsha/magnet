package app.extension;

import magnet.DependencyScope;
import magnet.internal.Factory;

public final class MagnetUnknownTypeTabFactory implements Factory<Tab> {
    @Override
    public Tab create(DependencyScope dependencyScope) {
        return new UnknownTypeTab();
    }

    public static Class getType() {
        return Tab.class;
    }
}