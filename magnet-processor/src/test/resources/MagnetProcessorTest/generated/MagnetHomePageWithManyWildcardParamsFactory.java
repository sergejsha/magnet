package app.extension;

import app.HomeRepository;
import app.Page;
import java.util.List;
import magnet.InstanceFactory;
import magnet.Scope;
import magnet.Scoping;

public final class MagnetHomePageWithManyWildcardParamsFactory implements InstanceFactory<Page> {

    @Override
    public Page create(Scope scope) {
        List<HomeRepository> repositories = scope.getMany(HomeRepository.class);
        return new HomePageWithManyWildcardParams(repositories);
    }

    @Override
    public Scoping getScoping() {
        return Scoping.SCOPE;
    }

    public static Class getType() {
        return Page.class;
    }
}