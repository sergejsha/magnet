package app.extension;

import app.HomeRepository;
import app.Page;
import app.UserData;
import magnet.ScopeContainer;
import magnet.internal.InstanceFactory;

public final class HomePageWithClassifierParamsMagnetFactory extends InstanceFactory<Page> {

    @Override
    public Page create(ScopeContainer scope) {
        HomeRepository homeRepository = scope.getOptional(HomeRepository.class, "local");
        UserData userData = scope.getSingle(UserData.class, "global");
        return new HomePageWithClassifierParams(homeRepository, userData);
    }

    public static Class getType() {
        return Page.class;
    }
}