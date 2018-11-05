package selector;

import magnet.ScopeContainer;
import magnet.internal.InstanceFactory;

public final class Implementation8MagnetFactory extends InstanceFactory<Interface> {
    private static String[] SELECTOR = { "android", "api", "in", "1", "19" };

    @Override
    public Interface create(ScopeContainer scope) {
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