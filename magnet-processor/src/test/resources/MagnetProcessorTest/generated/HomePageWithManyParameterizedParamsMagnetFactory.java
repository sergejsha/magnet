package app.extension;

import app.Page;
import app.WorkProcessor;
import java.util.List;
import magnet.Scope;
import magnet.Scoping;
import magnet.internal.InstanceFactory;

public final class HomePageWithManyParameterizedParamsMagnetFactory implements InstanceFactory<Page> {
    @Override
    @SuppressWarnings("unchecked")
    public Page create(Scope scope) {
        List<WorkProcessor> variant1 = scope.getMany(WorkProcessor.class);
        List<WorkProcessor> variant2 = scope.getMany(WorkProcessor.class, "global");
        List<WorkProcessor> variant3 = scope.getMany(WorkProcessor.class);
        List<WorkProcessor> variant4 = scope.getMany(WorkProcessor.class, "global");
        return new HomePageWithManyParameterizedParams(variant1, variant2, variant3, variant4);
    }

    @Override
    public Scoping getScoping() {
        return Scoping.TOPMOST;
    }

    public static Class getType() {
        return Page.class;
    }
}
