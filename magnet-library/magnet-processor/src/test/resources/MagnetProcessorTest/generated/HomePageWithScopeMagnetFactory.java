package app.extension;

import app.Page;
import magnet.internal.InstanceFactory;
import magnet.internal.ScopeContainer;

public final class HomePageWithScopeMagnetFactory extends InstanceFactory<Page> {

    @Override
    public Page create(ScopeContainer scope) {
        return new HomePageWithScope(scope);
    }

    public static Class getType() {
        return Page.class;
    }
}