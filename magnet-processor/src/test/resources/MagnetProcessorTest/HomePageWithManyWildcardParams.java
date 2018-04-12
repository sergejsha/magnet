package app.extension;

import java.util.List;

import app.HomeRepository;
import app.Page;
import magnet.Implementation;

@Implementation(type = Page.class)
class HomePageWithManyWildcardParams implements Page {

    HomePageWithManyWildcardParams(
            List<? extends HomeRepository> repositories
    ) { }

    @Override
    public void show() {
        // nop
    }

}