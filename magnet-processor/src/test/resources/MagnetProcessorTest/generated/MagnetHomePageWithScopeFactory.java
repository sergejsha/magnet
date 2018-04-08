package app.extension;

import app.Page;
import magnet.Factory;
import magnet.Scope;

public final class MagnetHomePageWithScopeFactory implements Factory<Page> {

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