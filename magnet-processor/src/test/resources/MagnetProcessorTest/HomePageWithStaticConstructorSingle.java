package app.extension.utils;

import java.util.List;

import app.HomeRepository;
import app.Page;
import app.extension.HomePageWithStaticConstructor;
import magnet.Instance;
import magnet.Scoping;

public class HomePageWithStaticConstructorSingle {

    @Instance(
            type = app.Page.class,
            scoping = Scoping.UNSCOPED
    )
    public static Page create(List<? extends HomeRepository> repositories) {
        return new HomePageWithStaticConstructor(repositories);
    }

}