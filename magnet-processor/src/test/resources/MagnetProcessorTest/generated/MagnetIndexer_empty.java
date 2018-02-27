package magnet;

import java.util.HashMap;
import java.util.Map;
import magnet.internal.Factory;

public final class MagnetIndexer {

    public static void register(MagnetImplementationManager implementationManager) {
        Factory[] factories = new Factory[0];
        Map<Class, Object> index = new HashMap<>();
        implementationManager.register(factories, index);
    }

}