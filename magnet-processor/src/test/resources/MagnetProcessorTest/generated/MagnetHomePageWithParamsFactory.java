package app.extension;

import app.HomeRepository;
import app.Page;
import app.UserData;
import magnet.DependencyScope;
import magnet.internal.Factory;

public final class MagnetHomePageWithParamsFactory implements Factory<Page> {

    @Override
    public Page create(DependencyScope dependencyScope) {
        HomeRepository homeRepository = dependencyScope.get(HomeRepository.class);
        UserData userData = dependencyScope.require(UserData.class);
        return new HomePageWithParams(homeRepository, userData);
    }

    public static Class getType() {
        return Page.class;
    }
}