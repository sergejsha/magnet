package app.extension;

import app.Page;
import magnet.InstanceFactory;
import magnet.Scope;
import magnet.Scoping;

public final class HomePageNoParamsMagnetFactory implements InstanceFactory<Page> {

    @Override
    public Page create(Scope scope) {
        return new HomePageNoParams();
    }

    @Override
    public Scoping getScoping() {
        return Scoping.TOPMOST;
    }

    public static Class getType() {
        return Page.class;
    }
}