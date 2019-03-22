package test;

import magnet.Instance;

@Instance(
    type = Interface.class,
    disposer = "disposeIt"
)
public class Implementation2 implements Interface {

    void dispose() {}

}