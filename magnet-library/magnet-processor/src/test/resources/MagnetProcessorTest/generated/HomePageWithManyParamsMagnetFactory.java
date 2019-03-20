package app.extension;

import app.HomeRepository;
import app.Page;
import java.util.List;
import magnet.Scope;
import magnet.internal.Generated;
import magnet.internal.InstanceFactory;

@Generated
public final class HomePageWithManyParamsMagnetFactory extends InstanceFactory<Page> {

    @Override
    public Page create(Scope scope) {
        List<HomeRepository> variant1 = scope.getMany(HomeRepository.class, "");
        List<HomeRepository> variant2 = scope.getMany(HomeRepository.class, "global");
        List<HomeRepository> variant3 = scope.getMany(HomeRepository.class, "");
        List<HomeRepository> variant4 = scope.getMany(HomeRepository.class, "global");
        return new HomePageWithManyParams(variant1, variant2, variant3, variant4);
    }

    public static Class getType() {
        return Page.class;
    }
}