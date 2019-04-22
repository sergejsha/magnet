package app;

import magnet.Scope;
import magnet.internal.Generated;
import magnet.internal.InstanceFactory;

@Generated
public final class UnderTestMagnetFactory extends InstanceFactory<UnderTest> {
    @Override
    public UnderTest create(Scope scope) {
        String value1 = scope.getSingle(String.class, "");
        String value2 = scope.getSingle(String.class, "");
        return new UnderTest(value1, value2);
    }

    public static Class getType() {
        return UnderTest.class;
    }
}
