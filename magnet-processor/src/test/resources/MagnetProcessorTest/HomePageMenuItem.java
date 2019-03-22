package app.extension;

import app.MenuItem;
import magnet.Instance;
import magnet.Scope;

@Instance(
        type = MenuItem.class,
        classifier = "main-menu"
)
class HomePageMenuItem implements MenuItem {

    HomePageMenuItem(Scope scope) {
    }

    @Override
    public int getId() {
        return 1;
    }

}