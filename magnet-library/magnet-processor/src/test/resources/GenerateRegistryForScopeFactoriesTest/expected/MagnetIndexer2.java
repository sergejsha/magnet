package magnet.internal;

import java.util.HashMap;
import java.util.Map;
import test.Scope2_1MagnetFactory;
import test.Scope2_2MagnetFactory;
import test.Scope2_3MagnetFactory;

public final class MagnetIndexer {
    public static void register(MagnetInstanceManager instanceManager) {
        InstanceFactory[] factories = new InstanceFactory[0];
        Map<Class, Object> index = new HashMap<>(16);
        Map<Class, ScopeFactory> scopeFactories = new HashMap(4);
        scopeFactories.put(Scope2_1MagnetFactory.getType(), new Scope2_1MagnetFactory());
        scopeFactories.put(Scope2_2MagnetFactory.getType(), new Scope2_2MagnetFactory());
        scopeFactories.put(Scope2_3MagnetFactory.getType(), new Scope2_3MagnetFactory());
        instanceManager.register(factories, index, scopeFactories);
    }
}