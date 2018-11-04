package magnet.index;

import magnet.internal.Index;
import magnet.internal.ScopeFactory;
import test.MagnetScope1Factory;

@Index(
    factoryType = ScopeFactory.class,
    factoryClass = MagnetScope1Factory.class,
    instanceType = "test.Scope1",
    classifier = ""
)
public final class test_MagnetScope1Factory {
}