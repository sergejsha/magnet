package app.extension;

import app.MenuItem;
import magnet.Instance;
import magnet.Scope;

@Instance(
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