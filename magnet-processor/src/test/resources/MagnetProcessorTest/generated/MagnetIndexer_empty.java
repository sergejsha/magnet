package magnet;

import java.util.HashMap;
import java.util.Map;

public final class MagnetIndexer {

    public static void register(MagnetInstanceManager instanceManager) {
        Factory[] factories = new Factory[0];
        Map<Class, Object> index = new HashMap<>();
        instanceManager.register(factories, index);
    }

}