package magnet;

import app.extension.MagnetHomePageFactory;
import app.extension.MagnetHomePageMenuItemFactory;
import app.extension.MagnetUserPageFactory;
import app.extension.MagnetUserPageMenuItemFactory;

import java.util.HashMap;
import java.util.Map;

import magnet.index.app_extension_MagnetHomePageFactory;
import magnet.index.app_extension_MagnetUserPageMenuItemFactory;
import magnet.internal.Factory;
import magnet.internal.Range;

public final class MagnetIndexer {

    public static void register(MagnetImplementationManager implementationManager) {
        Factory[] factories = new Factory[] {
                new MagnetUserPageMenuItemFactory(),
                new MagnetHomePageMenuItemFactory(),
                new MagnetHomePageFactory(),
                new MagnetUserPageFactory(),
        };

        Map<Class, Object> index = new HashMap<>();

        Map<String, Range> ranges1 = new HashMap<>();
        ranges1.put("extended-menu", new Range(0, 1, "extended-menu"));
        ranges1.put("main-menu", new Range(1, 1, "main-menu"));

        index.put(app_extension_MagnetUserPageMenuItemFactory.getTypeClass(), ranges1);
        index.put(app_extension_MagnetHomePageFactory.getTypeClass(), new Range(2, 2, ""));

        implementationManager.register(factories, index);
    }

}