package selector;

import magnet.Scope;
import magnet.internal.Generated;
import magnet.internal.InstanceFactory;

@Generated
public final class Implementation8MagnetFactory extends InstanceFactory<Interface> {
    private static String[] SELECTOR = { "android", "api", "in", "1", "19" };

    @Override
    public Interface create(Scope scope) {
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