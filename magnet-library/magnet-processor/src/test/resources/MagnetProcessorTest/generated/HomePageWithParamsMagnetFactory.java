package app.extension;

import app.HomeRepository;
import app.Page;
import app.UserData;
import magnet.internal.InstanceFactory;
import magnet.internal.ScopeContainer;

public final class HomePageWithParamsMagnetFactory extends InstanceFactory<Page> {

    @Override
    public Page create(ScopeContainer scope) {
        HomeRepository homeRepository = scope.getOptional(HomeRepository.class);
        UserData userData = scope.getSingle(UserData.class);
        return new HomePageWithParams(homeRepository, userData);
    }

    public static Class getType() {
        return Page.class;
    }
}