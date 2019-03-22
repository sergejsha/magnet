package app.extension;

import java.util.List;

import app.HomeRepository;
import app.Page;

public class HomePageWithStaticConstructor implements Page {

    public HomePageWithStaticConstructor(
            List<? extends HomeRepository> repositories
    ) { }

    @Override
    public void show() {
        // nop
    }

}