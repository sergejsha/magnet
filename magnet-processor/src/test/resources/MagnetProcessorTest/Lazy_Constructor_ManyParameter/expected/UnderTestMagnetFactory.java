package app;

import java.util.List;
import kotlin.Lazy;
import magnet.Scope;
import magnet.internal.Generated;
import magnet.internal.InstanceFactory;
import magnet.internal.ManyLazy;

@Generated
public final class UnderTestMagnetFactory extends InstanceFactory<UnderTest> {
    @Override
    public UnderTest create(Scope scope) {
        Lazy<List<String>> dep = new ManyLazy(scope, String.class, "");
        return new UnderTest(dep);
    }

    public static Class getType() {
        return UnderTest.class;
    }
}