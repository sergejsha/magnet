package app.extension;

import app.HomeRepository;
import app.Page;
import app.UserData;
import magnet.InstanceFactory;
import magnet.InstanceRetention;
import magnet.Scope;

public final class MagnetHomePageWithClassifierParamsFactory implements InstanceFactory<Page> {

    @Override
    public Page create(Scope scope) {
        HomeRepository homeRepository = scope.getOptional(HomeRepository.class, "local");
        UserData userData = scope.getSingle(UserData.class, "global");
        return new HomePageWithClassifierParams(homeRepository, userData);
    }

    @Override
    public InstanceRetention getInstanceRetention() {
        return InstanceRetention.SCOPE;
    }

    public static Class getType() {
        return Page.class;
    }
}