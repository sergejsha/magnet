package selector;

import magnet.internal.InstanceFactory;
import magnet.internal.InstanceScope;

public final class Implementation8MagnetFactory extends InstanceFactory<Interface> {
    private static String[] SELECTOR = { "android", "api", "in", "1", "19" };

    @Override
    public Interface create(InstanceScope scope) {
        return new Implementation8();
    }

    @Override
    public String[] getSelector() {
        return SELECTOR;
    }

    public static Class getType() {
        return Interface.class;
    }
}