package app.extension;

import app.MenuItem;
import magnet.Scope;
import magnet.Implementation;

@Implementation(
        type = MenuItem.class,
        classifier = "extended-menu"
)
class UserPageMenuItem implements MenuItem {

    UserPageMenuItem(Scope registry) {
    }

    @Override
    public int getId() {
        return 0;
    }

}