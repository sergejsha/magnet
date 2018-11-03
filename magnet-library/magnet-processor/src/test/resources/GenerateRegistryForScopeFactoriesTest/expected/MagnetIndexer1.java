package magnet.internal;

import java.util.HashMap;
import java.util.Map;
import test.Scope1MagnetFactory;

public final class MagnetIndexer {
    public static void register(MagnetInstanceManager instanceManager) {
        InstanceFactory[] factories = new InstanceFactory[0];
        Map<Class, Object> index = new HashMap<>(16);
        Map<Class, ScopeFactory> scopeFactories = new HashMap(2);
        scopeFactories.put(Scope1MagnetFactory.getType(), new Scope1MagnetFactory());
        instanceManager.register(factories, index, scopeFactories);
    }
}