package app;

import java.util.List;
import magnet.Scope;
import magnet.internal.Generated;
import magnet.internal.InstanceFactory;

@Generated
public final class UnderTestMagnetFactory extends InstanceFactory<UnderTest> {
    @Override
    public UnderTest create(Scope scope) {
        List<Foo> dep = scope.getMany(Foo.class, "");
        return new UnderTest(dep);
    }

    public static Class getType() {
        return UnderTest.class;
    }
}
