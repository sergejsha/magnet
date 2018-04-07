package app.extension;

import app.MenuItem;
import magnet.DependencyScope;
import magnet.Implementation;

@Implementation(
        type = MenuItem.class,
        classifier = "extended-menu"
)
class UserPageMenuItem implements MenuItem {

    UserPageMenuItem(DependencyScope registry) {
    }

    @Override
    public int getId() {
        return 0;
    }

}