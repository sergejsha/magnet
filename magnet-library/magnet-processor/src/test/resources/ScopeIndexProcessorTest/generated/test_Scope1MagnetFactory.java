package magnet.index;

import magnet.internal.Index;
import magnet.internal.ScopeFactory;
import test.Scope1MagnetFactory;

@Index(
    factoryType = ScopeFactory.class,
    factoryClass = Scope1MagnetFactory.class,
    instanceType = "test.Scope1",
    classifier = ""
)
public final class test_Scope1MagnetFactory {
}