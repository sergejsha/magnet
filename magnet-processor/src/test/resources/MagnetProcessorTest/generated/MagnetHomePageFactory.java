package app.extension;

import app.HomeRepository;
import app.Page;
import app.UserData;
import magnet.Scope;
import magnet.internal.Factory;

public final class MagnetHomePageFactory implements Factory<Page> {

    @Override
    public Page create(Scope scope) {
        HomeRepository homeRepository = scope.require(HomeRepository.class);
        UserData userData = scope.require(UserData.class);
        return new HomePage(homeRepository, userData, scope);
    }

    public static Class getType() {
        return Page.class;
    }
}