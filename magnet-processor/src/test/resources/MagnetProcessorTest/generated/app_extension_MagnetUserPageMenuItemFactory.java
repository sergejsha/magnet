package magnet.index;

import app.extension.MagnetUserPageMenuItemFactory;
import magnet.internal.FactoryIndex;

@FactoryIndex(
        factory = MagnetUserPageMenuItemFactory.class,
        type = "app.MenuItem",
        target = "extended-menu"
)
public final class app_extension_MagnetUserPageMenuItemFactory {}