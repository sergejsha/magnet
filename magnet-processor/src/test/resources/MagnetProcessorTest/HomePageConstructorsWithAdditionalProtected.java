package app.extension;

import app.Page;
import magnet.Instance;
import magnet.Scope;

@Instance(type = Page.class)
class HomePageConstructorsWithAdditionalProtected implements Page {

    HomePageConstructorsWithAdditionalProtected(Scope scope) { }

    protected HomePageConstructorsWithAdditionalProtected() { }

    @Override
    public void show() {
        // nop
    }

}
