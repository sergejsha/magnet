package app.extension;

import app.Page;
import magnet.Instance;
import magnet.Scope;

@Instance(type = Page.class)
class HomePageConstructorsWithAdditionalPrivate implements Page {

    HomePageConstructorsWithAdditionalPrivate(Scope scope) { }

    private HomePageConstructorsWithAdditionalPrivate() { }

    @Override
    public void show() {
        // nop
    }

}
