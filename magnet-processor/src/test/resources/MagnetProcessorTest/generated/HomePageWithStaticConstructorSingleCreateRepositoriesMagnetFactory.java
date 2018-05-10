package app.extension.utils;

import app.HomeRepository;
import app.Page;
import java.util.List;
import magnet.InstanceFactory;
import magnet.Scope;
import magnet.Scoping;

public final class HomePageWithStaticConstructorSingleCreateRepositoriesMagnetFactory implements InstanceFactory<Page> {

    @Override
    public Page create(Scope scope) {
        List<HomeRepository> repositories = scope.getMany(HomeRepository.class);
        return HomePageWithStaticConstructorSingle.create(repositories);
    }

    @Override
    public Scoping getScoping() {
        return Scoping.UNSCOPED;
    }

    public static Class getType() {
        return Page.class;
    }

}