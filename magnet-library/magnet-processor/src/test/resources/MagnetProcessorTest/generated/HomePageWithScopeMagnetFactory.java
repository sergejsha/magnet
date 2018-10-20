package app.extension;

import app.Page;
import magnet.Scope;
import magnet.Scoping;
import magnet.internal.InstanceFactory;

public final class HomePageWithScopeMagnetFactory extends InstanceFactory<Page> {

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