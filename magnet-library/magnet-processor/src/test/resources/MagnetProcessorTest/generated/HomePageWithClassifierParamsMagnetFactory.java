package app.extension;

import app.HomeRepository;
import app.Page;
import app.UserData;
import magnet.internal.InstanceFactory;
import magnet.internal.InstanceScope;

public final class HomePageWithClassifierParamsMagnetFactory extends InstanceFactory<Page> {

    @Override
    public Page create(InstanceScope scope) {
        HomeRepository homeRepository = scope.getOptional(HomeRepository.class, "local");
        UserData userData = scope.getSingle(UserData.class, "global");
        return new HomePageWithClassifierParams(homeRepository, userData);
    }

    public static Class getType() {
        return Page.class;
    }
}