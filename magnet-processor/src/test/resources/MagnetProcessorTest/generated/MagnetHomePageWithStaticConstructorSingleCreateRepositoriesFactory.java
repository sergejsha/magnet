package app.extension.utils;

import app.HomeRepository;
import app.Page;
import java.util.List;
import magnet.InstanceFactory;
import magnet.InstanceRetention;
import magnet.Scope;

public final class MagnetHomePageWithStaticConstructorSingleCreateRepositoriesFactory implements InstanceFactory<Page> {

    @Override
    public Page create(Scope scope) {
        List<HomeRepository> repositories = scope.getMany(HomeRepository.class);
        return HomePageWithStaticConstructorSingle.create(repositories);
    }

    @Override
    public InstanceRetention getInstanceRetention() {
        return InstanceRetention.NONE;
    }

    public static Class getType() {
        return Page.class;
    }

}