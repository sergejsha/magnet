package magnet.internal;

import app.extension.UnknownTypeTab2MagnetFactory;
import app.extension.UnknownTypeTabMagnetFactory;
import java.util.HashMap;
import java.util.Map;

public final class MagnetIndexer {
    public static void register(MagnetInstanceManager instanceManager) {
        InstanceFactory[] factories = new InstanceFactory[] {
                new UnknownTypeTabMagnetFactory(),
                new UnknownTypeTab2MagnetFactory(),
        };
        Map<Class, Object> index = new HashMap<>(16);
        Map<String, Range> ranges1 = new HashMap<>(8);
        ranges1.put("", new Range(0, 1, ""));
        ranges1.put("2", new Range(1, 1, "2"));
        index.put(UnknownTypeTabMagnetFactory.getType(), ranges1);
        instanceManager.register(factories, index, null);
    }
}