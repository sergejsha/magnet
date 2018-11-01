package app.extension;

import magnet.internal.InstanceFactory;
import magnet.internal.ScopeContainer;

public final class UnknownTypeTab2MagnetFactory extends InstanceFactory<Tab> {
    @Override
    public Tab create(ScopeContainer scope) {
        return new UnknownTypeTab2();
    }

    public static Class getType() {
        return Tab.class;
    }
}