package selector;

import magnet.Scope;
import magnet.internal.Generated;
import magnet.internal.InstanceFactory;

@Generated
public final class Implementation10MagnetFactory extends InstanceFactory<Interface> {
    private static String[] SELECTOR = { "android", "api", "!in", "5", "10" };

    @Override
    public Interface create(Scope scope) {
        return new Implementation10();
    }

    @Override
    public String[] getSelector() {
        return SELECTOR;
    }

    public static Class getType() {
        return Interface.class;
    }
}