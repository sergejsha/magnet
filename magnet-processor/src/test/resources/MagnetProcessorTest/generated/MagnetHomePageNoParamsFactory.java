package app.extension;

import app.Page;
import magnet.DependencyScope;
import magnet.internal.Factory;

public final class MagnetHomePageNoParamsFactory implements Factory<Page> {

    @Override
    public Page create(DependencyScope dependencyScope) {
        return new HomePageNoParams();
    }

}