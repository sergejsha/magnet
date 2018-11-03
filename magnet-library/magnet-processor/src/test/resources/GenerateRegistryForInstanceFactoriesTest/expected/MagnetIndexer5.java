package magnet.internal;

import java.util.HashMap;
import java.util.Map;
import test.Implementation5_1MagnetFactory;
import test.Implementation5_2MagnetFactory;

public final class MagnetIndexer {
    public static void register(MagnetInstanceManager instanceManager) {
        InstanceFactory[] factories = new InstanceFactory[] {
            new Implementation5_1MagnetFactory(),
            new Implementation5_2MagnetFactory(),
        };
        Map<Class, Object> index = new HashMap<>(16);
        index.put(Implementation5_1MagnetFactory.getType(), new Range(0, 2, "zero"));
        Map<Class, ScopeFactory> scopeFactories = new HashMap(0);
        instanceManager.register(factories, index, scopeFactories);
    }
}