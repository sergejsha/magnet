package app.extension;

import app.HomeRepository;
import app.Page;
import java.util.List;
import magnet.internal.InstanceFactory;
import magnet.internal.InstanceScope;

public final class HomePageWithManyWildcardParamsMagnetFactory extends InstanceFactory<Page> {

    @Override
    public Page create(InstanceScope scope) {
        List<HomeRepository> repositories = scope.getMany(HomeRepository.class);
        return new HomePageWithManyWildcardParams(repositories);
    }

    public static Class getType() {
        return Page.class;
    }
}