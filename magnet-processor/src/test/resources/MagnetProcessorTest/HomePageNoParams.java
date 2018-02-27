package app.extension;

import app.Page;
import magnet.Implementation;

@Implementation(forType = Page.class)
class HomePageNoParams implements Page {

    HomePageNoParams() { }

    @Override
    public void show() {
        // nop
    }

}