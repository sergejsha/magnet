package app.extension;

import app.MenuItem;
import magnet.DependencyScope;
import magnet.Implementation;

@Implementation(
        forType = MenuItem.class,
        forTarget = "main-menu"
)
class HomePageMenuItem implements MenuItem {

    HomePageMenuItem(DependencyScope dependencyScope) {
    }

    @Override
    public int getId() {
        return 1;
    }

}