package magnet.internal;

import java.util.HashMap;
import java.util.Map;
import magnet.SelectorFilter;
import selector.Implementation1MagnetFactory;
import selector.Implementation2MagnetFactory;
import selector.SimpleSelectorFilter;

public final class MagnetIndexer {
    public static void register(MagnetInstanceManager instanceManager) {
        InstanceFactory[] factories = new InstanceFactory[] {
            new Implementation1MagnetFactory(),
            new Implementation2MagnetFactory(),
        };
        Map<Class, Object> index = new HashMap<>(16);
        index.put(Implementation1MagnetFactory.getType(), new Range(0, 1, ""));
        index.put(Implementation2MagnetFactory.getType(), new Range(1, 1, ""));
        Map<String, SelectorFilter> filters = new HashMap<>(1);
        SelectorFilter filter0 = new SimpleSelectorFilter();
        filters.put(filter0.getId(), filter0);
        instanceManager.register(factories, index, filters);
    }
}