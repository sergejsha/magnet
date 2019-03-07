package app;

import kotlin.Lazy;
import magnet.Scope;
import magnet.internal.Generated;
import magnet.internal.InstanceFactory;
import magnet.internal.OptionalLazy;
import magnet.internal.SingleLazy;

@Generated
public final class UnderTestMagnetFactory extends InstanceFactory<UnderTest> {
    @Override
    public UnderTest create(Scope scope) {
        Lazy<String> dep1 = new SingleLazy(scope, String.class, "");
        Lazy<String> dep2 = new OptionalLazy(scope, String.class, "");
        return new UnderTest(dep1, dep2);
    }

    public static Class getType() {
        return UnderTest.class;
    }
}
