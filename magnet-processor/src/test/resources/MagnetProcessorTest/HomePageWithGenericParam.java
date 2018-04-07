package app.extension;

import app.Page;
import magnet.Implementation;

@Implementation(type = Page.class)
class HomePageWithGenericParam<T extends Object> implements Page {

    HomePageWithGenericParam(
            T genericThing
    ) { }

    @Override
    public void show() {
        // nop
    }

}