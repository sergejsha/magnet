package magnet.internal;

import java.util.HashMap;
import java.util.Map;
import test.MagnetScope2_1Factory;
import test.MagnetScope2_2Factory;
import test.MagnetScope2_3Factory;

public final class MagnetIndexer {
    public static void register(MagnetInstanceManager instanceManager) {
        InstanceFactory[] factories = new InstanceFactory[0];
        Map<Class, Object> index = new HashMap<>(16);
        Map<Class, ScopeFactory> scopeFactories = new HashMap(4);
        scopeFactories.put(MagnetScope2_1Factory.getType(), new MagnetScope2_1Factory());
        scopeFactories.put(MagnetScope2_2Factory.getType(), new MagnetScope2_2Factory());
        scopeFactories.put(MagnetScope2_3Factory.getType(), new MagnetScope2_3Factory());
        instanceManager.register(factories, index, scopeFactories);
    }
}