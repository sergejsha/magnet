package selector;

import magnet.Scope;
import magnet.internal.Generated;
import magnet.internal.InstanceFactory;

@Generated
public final class Implementation9MagnetFactory extends InstanceFactory<Interface> {
    private static String[] SELECTOR = { "android", "api", "!=", "19" };

    @Override
    public Interface create(Scope scope) {
        return new Implementation9();
    }

    @Override
    public String[] getSelector() {
        return SELECTOR;
    }

    public static Class getType() {
        return Interface.class;
    }
}