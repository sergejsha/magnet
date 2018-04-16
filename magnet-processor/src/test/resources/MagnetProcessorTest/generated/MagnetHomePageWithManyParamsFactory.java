package app.extension;

import app.HomeRepository;
import app.Page;
import java.util.List;
import magnet.InstanceFactory;
import magnet.Scope;
import magnet.Scoping;

public final class MagnetHomePageWithManyParamsFactory implements InstanceFactory<Page> {

    @Override
    public Page create(Scope scope) {
        List<HomeRepository> variant1 = scope.getMany(HomeRepository.class);
        List<HomeRepository> variant2 = scope.getMany(HomeRepository.class, "global");
        List<HomeRepository> variant3 = scope.getMany(HomeRepository.class);
        List<HomeRepository> variant4 = scope.getMany(HomeRepository.class, "global");
        return new HomePageWithManyParams(variant1, variant2, variant3, variant4);
    }

    @Override
    public Scoping getScoping() {
        return Scoping.SCOPE;
    }

    public static Class getType() {
        return Page.class;
    }
}