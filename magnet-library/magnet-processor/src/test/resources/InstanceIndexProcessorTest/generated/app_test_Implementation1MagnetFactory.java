package magnet.index;

import app.test.Implementation1MagnetFactory;
import magnet.internal.Index;
import magnet.internal.InstanceFactory;

@Index(
    factoryType = InstanceFactory.class,
    factoryClass = Implementation1MagnetFactory.class,
    instanceType = "app.Interface1",
    classifier = "implementation1"
)
public final class app_test_Implementation1MagnetFactory {}