package app.extension;

import app.Page;
import magnet.ScopeContainer;
import magnet.internal.InstanceFactory;

public final class HomePageNoParamsMagnetFactory extends InstanceFactory<Page> {

    @Override
    public Page create(ScopeContainer scope) {
        return new HomePageNoParams();
    }

    public static Class getType() {
        return Page.class;
    }
}