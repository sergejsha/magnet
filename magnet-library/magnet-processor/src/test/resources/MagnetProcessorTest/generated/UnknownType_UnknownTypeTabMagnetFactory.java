package app.extension;

import magnet.internal.InstanceFactory;
import magnet.internal.ScopeContainer;

public final class UnknownTypeTabMagnetFactory extends InstanceFactory<Tab> {
    @Override
    public Tab create(ScopeContainer scope) {
        return new UnknownTypeTab();
    }

    public static Class getType() {
        return Tab.class;
    }
}