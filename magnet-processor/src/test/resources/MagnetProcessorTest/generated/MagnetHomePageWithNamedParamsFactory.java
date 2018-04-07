package app.extension;

import app.HomeRepository;
import app.Page;
import app.UserData;
import magnet.Scope;
import magnet.internal.Factory;

public final class MagnetHomePageWithNamedParamsFactory implements Factory<Page> {

    @Override
    public Page create(Scope scope) {
        HomeRepository homeRepository = scope.get(HomeRepository.class, "local");
        UserData userData = scope.require(UserData.class, "global");
        return new HomePageWithNamedParams(homeRepository, userData);
    }

    public static Class getType() {
        return Page.class;
    }
}