package app.extension;

import app.Page;
import magnet.InstanceFactory;
import magnet.Scope;

public final class MagnetHomePageWithScopeFactory implements InstanceFactory<Page> {

    @Override
    public Page create(Scope scope) {
        return new HomePageWithScope(scope);
    }

    @Override
    public boolean isScoped() {
        return true;
    }

    public static Class getType() {
        return Page.class;
    }
}