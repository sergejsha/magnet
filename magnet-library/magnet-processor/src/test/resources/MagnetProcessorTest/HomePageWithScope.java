package app.extension;

import app.Page;
import magnet.Instance;
import magnet.internal.ScopeContainer;

@Instance(type = Page.class)
class HomePageWithScope implements Page {

    HomePageWithScope(ScopeContainer scope) { }

    @Override
    public void show() {
        // nop
    }

}