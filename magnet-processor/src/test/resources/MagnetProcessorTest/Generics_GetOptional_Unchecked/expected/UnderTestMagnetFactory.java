package app;

import magnet.Scope;
import magnet.internal.Generated;
import magnet.internal.InstanceFactory;

@Generated
public final class UnderTestMagnetFactory extends InstanceFactory<UnderTest> {

    @Override
    @SuppressWarnings("unchecked")
    public UnderTest create(Scope scope) {
        Dependency<Thread> dependency = scope.getOptional(Dependency.class, "");
        return new UnderTest(dependency);
    }

    public static Class getType() {
        return UnderTest.class;
    }
}