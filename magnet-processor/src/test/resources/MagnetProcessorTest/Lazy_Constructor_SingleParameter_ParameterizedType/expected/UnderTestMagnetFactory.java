package app;

import kotlin.Lazy;
import magnet.Scope;
import magnet.internal.Generated;
import magnet.internal.InstanceFactory;
import magnet.internal.SingleLazy;

@Generated
public final class UnderTestMagnetFactory extends InstanceFactory<UnderTest> {
    @Override
    public UnderTest create(Scope scope) {
        Lazy<Foo<String>> dep = new SingleLazy(scope, Foo.class, "");
        return new UnderTest(dep);
    }

    public static Class getType() {
        return UnderTest.class;
    }
}
