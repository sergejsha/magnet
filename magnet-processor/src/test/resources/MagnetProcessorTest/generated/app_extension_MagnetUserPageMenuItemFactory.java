package magnet.index;

import app.extension.MagnetUserPageMenuItemFactory;
import magnet.internal.FactoryIndex;

@FactoryIndex(
        factory = MagnetUserPageMenuItemFactory.class,
        type = "app.MenuItem",
        classifier = "extended-menu"
)
public final class app_extension_MagnetUserPageMenuItemFactory {}