package test;

import magnet.Instance;

@Instance(
    type = Interface.class,
    disposer = "disposeIt"
)
public class Implementation4 implements Interface {

    String disposeIt() { return null; }

}