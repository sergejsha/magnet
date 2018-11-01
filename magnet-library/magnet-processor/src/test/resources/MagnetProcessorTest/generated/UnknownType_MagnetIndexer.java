package magnet.internal;

import app.extension.UnknownTypeTabMagnetFactory;

import java.util.HashMap;
import java.util.Map;

public final class MagnetIndexer {
    public static void register(MagnetInstanceManager instanceManager) {
        InstanceFactory[] factories = new InstanceFactory[]{
            new UnknownTypeTabMagnetFactory(),
        };
        Map<Class, Object> index = new HashMap<>(16);
        index.put(UnknownTypeTabMagnetFactory.getType(), new Range(0, 1, ""));
        instanceManager.register(factories, index, null);
    }
}
