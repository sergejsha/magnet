package app.extension;

import app.Page;
import magnet.Scope;
import magnet.internal.Factory;

public final class MagnetHomePageNoParamsFactory implements Factory<Page> {

    @Override
    public Page create(Scope scope) {
        return new HomePageNoParams();
    }

    public static Class getType() {
        return Page.class;
    }
}