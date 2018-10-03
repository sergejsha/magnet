package app.extension;

import app.Page;
import app.WorkProcessor;
import java.util.List;
import magnet.Scope;
import magnet.Scoping;
import magnet.internal.InstanceFactory;

public final class HomePageWithManyParameterizedWildcardInParamsMagnetFactory implements InstanceFactory<Page> {
    @Override
    @SuppressWarnings("unchecked")
    public Page create(Scope scope) {
        List variant1 = scope.getMany(WorkProcessor.class);
        List variant2 = scope.getMany(WorkProcessor.class, "global");
        List variant3 = scope.getMany(WorkProcessor.class);
        List variant4 = scope.getMany(WorkProcessor.class, "global");
        return new HomePageWithManyParameterizedWildcardInParams(variant1, variant2, variant3, variant4);
    }

    @Override
    public Scoping getScoping() {
        return Scoping.TOPMOST;
    }

    public static Class getType() {
        return Page.class;
    }
}
