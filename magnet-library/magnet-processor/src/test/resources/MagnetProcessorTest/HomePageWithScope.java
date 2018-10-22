package app.extension;

import app.Page;
import magnet.Instance;
import magnet.Scope;

@Instance(type = Page.class)
class HomePageWithScope implements Page {

    HomePageWithScope(Scope scope) { }

    @Override
    public void show() {
        // nop
    }

}