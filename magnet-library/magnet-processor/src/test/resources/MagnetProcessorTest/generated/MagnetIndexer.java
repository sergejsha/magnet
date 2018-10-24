package magnet.internal;

import app.extension.HomePageMagnetFactory;
import app.extension.HomePageMenuItemMagnetFactory;
import app.extension.UserPageMagnetFactory;
import app.extension.UserPageMenuItemMagnetFactory;

import java.util.HashMap;
import java.util.Map;

public final class MagnetIndexer {

    public static void register(MagnetInstanceManager instanceManager) {
        InstanceFactory[] factories = new InstanceFactory[]{
            new UserPageMenuItemMagnetFactory(),
            new HomePageMenuItemMagnetFactory(),
            new HomePageMagnetFactory(),
            new UserPageMagnetFactory(),
        };

        Map<Class, Object> index = new HashMap<>(16);

        Map<String, Range> ranges1 = new HashMap<>(8);
        ranges1.put("extended-menu", new Range(0, 1, "extended-menu"));
        ranges1.put("main-menu", new Range(1, 1, "main-menu"));

        index.put(UserPageMenuItemMagnetFactory.getType(), ranges1);
        index.put(HomePageMagnetFactory.getType(), new Range(2, 2, ""));

        instanceManager.register(factories, index, null);
    }

}