package app.extension;

import app.Page;
import magnet.internal.InstanceFactory;
import magnet.internal.InstanceScope;

public final class HomePageNoParamsMagnetFactory extends InstanceFactory<Page> {

    @Override
    public Page create(InstanceScope scope) {
        return new HomePageNoParams();
    }

    public static Class getType() {
        return Page.class;
    }
}