package magnet.internal;

import java.util.HashMap;
import java.util.Map;
import test.Implementation3_1MagnetFactory;
import test.Implementation3_2MagnetFactory;

public final class MagnetIndexer {
    public static void register(MagnetInstanceManager instanceManager) {
        InstanceFactory[] factories = new InstanceFactory[] {
            new Implementation3_1MagnetFactory(),
            new Implementation3_2MagnetFactory(),
        };
        Map<Class, Object> index = new HashMap<>(16);
        index.put(Implementation3_1MagnetFactory.getType(), new Range(0, 2, ""));
        Map<Class, ScopeFactory> scopeFactories = null;
        instanceManager.register(factories, index, scopeFactories);
    }
}