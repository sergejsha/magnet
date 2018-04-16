package app.extension;

import app.HomeRepository;
import app.Page;
import app.UserData;
import magnet.InstanceFactory;
import magnet.Scope;
import magnet.Scoping;

public final class MagnetHomePageWithParamsFactory implements InstanceFactory<Page> {

    @Override
    public Page create(Scope scope) {
        HomeRepository homeRepository = scope.getOptional(HomeRepository.class);
        UserData userData = scope.getSingle(UserData.class);
        return new HomePageWithParams(homeRepository, userData);
    }

    @Override
    public Scoping getScoping() {
        return Scoping.SCOPE;
    }

    public static Class getType() {
        return Page.class;
    }
}