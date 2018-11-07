package magnet.internal;

import java.util.HashMap;
import java.util.Map;
import test.Implementation6_1MagnetFactory;
import test.Implementation6_2MagnetFactory;

public final class MagnetIndexer {
    public static void register(MagnetInstanceManager instanceManager) {
        InstanceFactory[] factories = new InstanceFactory[] {
            new Implementation6_1MagnetFactory(),
            new Implementation6_2MagnetFactory(),
        };
        Map<Class, Object> index = new HashMap<>(16);
        index.put(Implementation6_1MagnetFactory.getType(), new Range(0, 1, ""));
        index.put(Implementation6_2MagnetFactory.getType(), new Range(1, 1, ""));
        instanceManager.register(factories, index);
    }
}