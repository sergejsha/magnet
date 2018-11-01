package app.extension;

import magnet.internal.InstanceFactory;
import magnet.internal.InstanceScope;

public final class UnknownTypeTabMagnetFactory extends InstanceFactory<Tab> {
    @Override
    public Tab create(InstanceScope scope) {
        return new UnknownTypeTab();
    }

    public static Class getType() {
        return Tab.class;
    }
}