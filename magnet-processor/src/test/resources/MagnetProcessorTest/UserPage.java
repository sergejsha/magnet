package app.extension;

import app.Page;
import magnet.DependencyScope;
import magnet.Implementation;

@Implementation(type = Page.class)
class UserPage implements Page {

    UserPage(DependencyScope registry) {
    }

    @Override
    public void show() {
        // nop
    }

}