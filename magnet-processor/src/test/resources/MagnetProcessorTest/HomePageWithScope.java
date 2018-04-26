package app.extension;

import app.Page;
import magnet.Implementation;
import magnet.Scope;

@Implementation(type = Page.class)
class HomePageWithScope implements Page {

    HomePageWithScope(Scope scope) { }

    @Override
    public void show() {
        // nop
    }

}