package app.extension;

import app.HomeRepository;
import app.Page;
import app.UserData;
import magnet.Factory;
import magnet.Scope;

public final class MagnetHomePageWithParamsFactory implements Factory<Page> {

    @Override
    public Page create(Scope scope) {
        HomeRepository homeRepository = scope.getOptional(HomeRepository.class);
        UserData userData = scope.getSingle(UserData.class);
        return new HomePageWithParams(homeRepository, userData);
    }

    @Override
    public boolean isScoped() {
        return true;
    }

    public static Class getType() {
        return Page.class;
    }
}