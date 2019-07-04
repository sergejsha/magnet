package app;

import magnet.Scope;
import magnet.internal.Generated;
import magnet.internal.InstanceFactory;

@Generated
public final class UnderTestMagnetFactory extends InstanceFactory<UnderTest> {
    @Override
    public UnderTest create(Scope scope) {
        return new UnderTest(scope);
    }

    @Override
    public String getLimit() {
        return "activity";
    }

    public static Class getType() {
        return UnderTest.class;
    }
}