package app.extension;

import app.Page;
import magnet.Scope;
import magnet.Implementation;

@Implementation(type = Page.class)
class UserPage implements Page {

    UserPage(Scope registry) {
    }

    @Override
    public void show() {
        // nop
    }

}