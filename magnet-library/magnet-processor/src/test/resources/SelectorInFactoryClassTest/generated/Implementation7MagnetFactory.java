package selector;

import magnet.internal.InstanceFactory;
import magnet.internal.InstanceScope;

public final class Implementation7MagnetFactory extends InstanceFactory<Interface> {
    private static String[] SELECTOR = { "android", "api", ">=", "28" };

    @Override
    public Interface create(InstanceScope scope) {
        return new Implementation7();
    }

    @Override
    public String[] getSelector() {
        return SELECTOR;
    }

    public static Class getType() {
        return Interface.class;
    }
}