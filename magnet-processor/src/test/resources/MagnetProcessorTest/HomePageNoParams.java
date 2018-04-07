package app.extension;

import app.Page;
import magnet.Implementation;

@Implementation(type = Page.class)
class HomePageNoParams implements Page {

    HomePageNoParams() { }

    @Override
    public void show() {
        // nop
    }

}