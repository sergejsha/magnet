package app.extension;

import app.Page;
import magnet.Instance;
import magnet.internal.InstanceScope;

@Instance(type = Page.class)
class HomePageWithScope implements Page {

    HomePageWithScope(InstanceScope scope) { }

    @Override
    public void show() {
        // nop
    }

}