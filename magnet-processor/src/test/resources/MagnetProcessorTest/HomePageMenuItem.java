package app.extension;

import app.MenuItem;
import magnet.DependencyScope;
import magnet.Implementation;

@Implementation(
        type = MenuItem.class,
        classifier = "main-menu"
)
class HomePageMenuItem implements MenuItem {

    HomePageMenuItem(DependencyScope dependencyScope) {
    }

    @Override
    public int getId() {
        return 1;
    }

}