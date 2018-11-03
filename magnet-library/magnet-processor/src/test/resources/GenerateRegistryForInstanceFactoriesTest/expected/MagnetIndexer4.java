package magnet.internal;

import java.util.HashMap;
import java.util.Map;
import test.Implementation4_1MagnetFactory;
import test.Implementation4_2MagnetFactory;

public final class MagnetIndexer {
    public static void register(MagnetInstanceManager instanceManager) {
        InstanceFactory[] factories = new InstanceFactory[] {
            new Implementation4_1MagnetFactory(),
            new Implementation4_2MagnetFactory(),
        };
        Map<Class, Object> index = new HashMap<>(16);
        Map<String, Range> ranges1 = new HashMap<>(8);
        ranges1.put("one", new Range(0, 1, "one"));
        ranges1.put("two", new Range(1, 1, "two"));
        index.put(Implementation4_1MagnetFactory.getType(), ranges1);
        Map<Class, ScopeFactory> scopeFactories = new HashMap(0);
        instanceManager.register(factories, index, scopeFactories);
    }