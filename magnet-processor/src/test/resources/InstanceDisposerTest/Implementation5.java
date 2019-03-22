package test;

import magnet.Instance;
import magnet.Scoping;

@Instance(
    type = Interface.class,
    scoping = Scoping.UNSCOPED,
    disposer = "disposeIt"
)
public class Implementation5 implements Interface {

    void disposeIt() { return null; }

}