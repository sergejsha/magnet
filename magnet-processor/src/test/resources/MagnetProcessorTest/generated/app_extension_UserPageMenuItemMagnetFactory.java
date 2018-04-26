package magnet.index;

import app.extension.UserPageMenuItemMagnetFactory;
import magnet.internal.FactoryIndex;

@FactoryIndex(
        factory = UserPageMenuItemMagnetFactory.class,
        type = "app.MenuItem",
        classifier = "extended-menu"
)
public final class app_extension_UserPageMenuItemMagnetFactory {}