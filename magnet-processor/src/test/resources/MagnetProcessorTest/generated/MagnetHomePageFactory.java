package app.extension;

import app.HomeRepository;
import app.Page;
import app.UserData;
import magnet.DependencyScope;
import magnet.internal.Factory;

public final class MagnetHomePageFactory implements Factory<Page> {

    @Override
    public Page create(DependencyScope dependencyScope) {
        HomeRepository homeRepository = dependencyScope.require(HomeRepository.class);
        UserData userData = dependencyScope.require(UserData.class);
        return new HomePage(homeRepository, userData, dependencyScope);
    }

}