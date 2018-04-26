package magnet;

import app.extension.UnknownTypeTabMagnetFactory;
import java.util.HashMap;
import java.util.Map;

import magnet.internal.Range;

public final class MagnetIndexer {
    public static void register(MagnetInstanceManager instanceManager) {
        InstanceFactory[] factories = new InstanceFactory[] {
                new UnknownTypeTabMagnetFactory(),
        };
        Map<Class, Object> index = new HashMap<>();
        index.put(UnknownTypeTabMagnetFactory.getType(), new Range(0, 1, ""));
        instanceManager.register(factories, index);
    }
}
