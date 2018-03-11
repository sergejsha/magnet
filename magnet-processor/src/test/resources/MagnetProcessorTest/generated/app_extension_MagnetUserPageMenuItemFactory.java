package magnet.index;

import app.MenuItem;
import app.extension.MagnetUserPageMenuItemFactory;
import magnet.internal.FactoryIndex;

@FactoryIndex(
        factory = MagnetUserPageMenuItemFactory.class,
        type = MenuItem.class,
        target = "extended-menu"
)
public final class app_extension_MagnetUserPageMenuItemFactory {
    public static Class getTypeClass() {
        return MenuItem.class;
    }
}