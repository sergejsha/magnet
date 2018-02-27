package magnet.index;

import app.Page;
import app.extension.MagnetHomePageFactory;
import magnet.internal.FactoryIndex;

@FactoryIndex(
        factory = MagnetHomePageFactory.class,
        type = Page.class,
        target = ""
)
public final class app_extension_MagnetHomePageFactory {}