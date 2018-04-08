package app.extension;

import app.Page;
import magnet.Scope;
import magnet.internal.Factory;

public final class MagnetHomePageNoParamsFactory implements Factory<Page> {

    @Override
    public Page create(Scope scope) {
        return new HomePageNoParams();
    }

    @Override
    public boolean isScoped() {
        return true;
    }

    public static Class getType() {
        return Page.class;
    }
}