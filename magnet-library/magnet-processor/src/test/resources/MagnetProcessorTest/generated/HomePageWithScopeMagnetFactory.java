package app.extension;

import app.Page;
import magnet.Scope;
import magnet.internal.InstanceFactory;

public final class HomePageWithScopeMagnetFactory extends InstanceFactory<Page> {

    @Override
    public Page create(Scope scope) {
        return new HomePageWithScope(scope);
    }

    public static Class getType() {
        return Page.class;
    }
}