package app.extension.utils;

import app.HomeRepository;
import app.Page;
import java.util.List;
import magnet.ScopeContainer;
import magnet.Scoping;
import magnet.internal.InstanceFactory;

public final class HomePageWithStaticConstructorSingleCreateRepositoriesMagnetFactory extends InstanceFactory<Page> {

    @Override
    public Page create(ScopeContainer scope) {
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