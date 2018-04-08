package app.extension;

import app.HomeRepository;
import app.Page;
import app.UserData;
import magnet.InstanceFactory;
import magnet.Scope;

public final class MagnetHomePageFactory implements InstanceFactory<Page> {

    @Override
    public Page create(Scope scope) {
        HomeRepository homeRepository = scope.getSingle(HomeRepository.class);
        UserData userData = scope.getSingle(UserData.class);
        return new HomePage(homeRepository, userData, scope);
    }

    @Override
    public boolean isScoped() {
        return true;
    }

    public static Class getType() {
        return Page.class;
    }
}