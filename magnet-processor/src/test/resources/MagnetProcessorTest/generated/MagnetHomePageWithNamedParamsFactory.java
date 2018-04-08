package app.extension;

import app.HomeRepository;
import app.Page;
import app.UserData;
import magnet.Scope;
import magnet.internal.Factory;

public final class MagnetHomePageWithNamedParamsFactory implements Factory<Page> {

    @Override
    public Page create(Scope scope) {
        HomeRepository homeRepository = scope.getOptional(HomeRepository.class, "local");
        UserData userData = scope.getSingle(UserData.class, "global");
        return new HomePageWithNamedParams(homeRepository, userData);
    }

    @Override
    public boolean isScoped() {
        return true;
    }

    public static Class getType() {
        return Page.class;
    }
}