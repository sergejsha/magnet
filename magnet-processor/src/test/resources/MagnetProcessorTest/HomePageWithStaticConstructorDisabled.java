package app.extension.utils;

import java.util.List;

import app.HomeRepository;
import app.Page;
import app.extension.HomePageWithStaticConstructor;
import magnet.Implementation;
import magnet.Scoping;

public class HomePageWithStaticConstructorDisabled {

    @Implementation(
            type = app.Page.class,
            scoping = Scoping.NONE,
            disabled = true
    )
    public static Page create(List<? extends HomeRepository> repositories) {
        return new HomePageWithStaticConstructor(repositories);
    }

}