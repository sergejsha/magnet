package app.extension;

import magnet.internal.InstanceFactory;
import magnet.internal.InstanceScope;

public final class UnknownTypeTab2MagnetFactory extends InstanceFactory<Tab> {
    @Override
    public Tab create(InstanceScope scope) {
        return new UnknownTypeTab2();
    }

    public static Class getType() {
        return Tab.class;
    }
}