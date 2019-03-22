package test;

import magnet.Instance;

@Instance(
    type = Interface.class,
    disposer = "disposeIt"
)
public class Implementation1 implements Interface {

    void disposeIt() {}

}