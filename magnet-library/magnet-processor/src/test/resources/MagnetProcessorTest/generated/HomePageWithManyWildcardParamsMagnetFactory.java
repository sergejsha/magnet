package app.extension;

import app.HomeRepository;
import app.Page;
import java.util.List;

import magnet.Scope;
import magnet.internal.InstanceFactory;

public final class HomePageWithManyWildcardParamsMagnetFactory extends InstanceFactory<Page> {

    @Override
    public Page create(Scope scope) {
        List<HomeRepository> repositories = scope.getMany(HomeRepository.class);
        return new HomePageWithManyWildcardParams(repositories);
    }

    public static Class getType() {
        return Page.class;
    }
}