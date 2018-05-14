package app.extension;

import app.Page;
import magnet.Instance;
import magnet.Scope;

@Instance(type = Page.class)
class UserPage implements Page {

    UserPage(Scope registry) {
    }

    @Override
    public void show() {
        // nop
    }

}