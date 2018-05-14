package app.extension;

import app.Page;
import magnet.Instance;

@Instance(type = Page.class)
class HomePageNoParams implements Page {

    HomePageNoParams() { }

    @Override
    public void show() {
        // nop
    }

}