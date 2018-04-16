package app.extension;

import app.Page;
import magnet.InstanceFactory;
import magnet.Scope;
import magnet.Scoping;

public final class MagnetHomePageWithScopeFactory implements InstanceFactory<Page> {

    @Override
    public Page create(Scope scope) {
        return new HomePageWithScope(scope);
    }

    @Override
    public Scoping getScoping() {
        return Scoping.TOPMOST;
    }

    public static Class getType() {
        return Page.class;
    }
}