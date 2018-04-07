package app.extension;

import app.Page;
import magnet.Scope;
import magnet.internal.Factory;

public final class MagnetHomePageWithScopeFactory implements Factory<Page> {

    @Override
    public Page create(Scope scope) {
        return new HomePageWithScope(scope);
    }

    public static Class getType() {
        return Page.class;
    }
}