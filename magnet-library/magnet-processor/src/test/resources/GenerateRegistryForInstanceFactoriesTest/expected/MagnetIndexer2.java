package magnet.internal;

import java.util.HashMap;
import java.util.Map;

public final class MagnetIndexer {
    public static void register(MagnetInstanceManager instanceManager) {
        InstanceFactory[] factories = new InstanceFactory[0];
        Map<Class, Object> index = new HashMap<>(16);
        Map<Class, ScopeFactory> scopeFactories = new HashMap(0);
        instanceManager.register(factories, index, scopeFactories);
    }
}