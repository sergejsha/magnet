package app.extension;

import java.util.List;

import app.HomeRepository;
import app.Page;
import magnet.Instance;

@Instance(type = Page.class)
class HomePageWithManyWildcardParams implements Page {

    HomePageWithManyWildcardParams(
            List<? extends HomeRepository> repositories
    ) { }

    @Override
    public void show() {
        // nop
    }

}