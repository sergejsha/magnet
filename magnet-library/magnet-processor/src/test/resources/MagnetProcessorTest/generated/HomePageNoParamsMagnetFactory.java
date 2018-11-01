package app.extension;

import app.Page;
import magnet.internal.InstanceFactory;
import magnet.internal.ScopeContainer;

public final class HomePageNoParamsMagnetFactory extends InstanceFactory<Page> {

    @Override
    public Page create(ScopeContainer scope) {
        return new HomePageNoParams();
    }

    public static Class getType() {
        return Page.class;
    }
}