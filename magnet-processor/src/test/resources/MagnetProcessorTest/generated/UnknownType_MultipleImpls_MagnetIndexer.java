package magnet;

import app.extension.MagnetUnknownTypeTab2Factory;
import app.extension.MagnetUnknownTypeTabFactory;
import java.util.HashMap;
import java.util.Map;
import magnet.internal.Factory;
import magnet.internal.Range;

public final class MagnetIndexer {
    public static void register(MagnetImplementationManager implementationManager) {
        Factory[] factories = new Factory[] {
                new MagnetUnknownTypeTabFactory(),
                new MagnetUnknownTypeTab2Factory(),
        };
        Map<Class, Object> index = new HashMap<>();
        Map<String, Range> ranges1 = new HashMap<>();
        ranges1.put("", new Range(0, 1, ""));
        ranges1.put("2", new Range(1, 1, "2"));
        index.put(MagnetUnknownTypeTabFactory.getType(), ranges1);
        implementationManager.register(factories, index);
    }
}