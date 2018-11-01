package app.extension;

import app.MenuItem;
import magnet.Instance;
import magnet.internal.ScopeContainer;

@Instance(
        type = MenuItem.class,
        classifier = "main-menu"
)
class HomePageMenuItem implements MenuItem {

    HomePageMenuItem(ScopeContainer scope) {
    }

    @Override
    public int getId() {
        return 1;
    }

}