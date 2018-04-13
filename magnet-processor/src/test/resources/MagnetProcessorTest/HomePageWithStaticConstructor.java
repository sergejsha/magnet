package app.extension;

import java.util.List;

import app.HomeRepository;
import app.Page;
import magnet.Implementation;

class HomePageWithStaticConstructor implements Page {

    private HomePageWithStaticConstructor(
            List<? extends HomeRepository> repositories
    ) { }

    @Override
    public void show() {
        // nop
    }

    @Implementation(type = Page.class)
    public static Page create(List<? extends HomeRepository> repositories) {
        return new HomePageWithStaticConstructor(repositories);
    }

}