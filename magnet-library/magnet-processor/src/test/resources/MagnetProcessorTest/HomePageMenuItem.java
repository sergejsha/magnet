package app.extension;

import app.MenuItem;
import magnet.Instance;
import magnet.internal.InstanceScope;

@Instance(
        type = MenuItem.class,
        classifier = "main-menu"
)
class HomePageMenuItem implements MenuItem {

    HomePageMenuItem(InstanceScope scope) {
    }

    @Override
    public int getId() {
        return 1;
    }

}