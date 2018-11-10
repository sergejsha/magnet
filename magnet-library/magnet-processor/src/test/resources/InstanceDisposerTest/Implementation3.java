package test;

import magnet.Instance;

@Instance(
    type = Interface.class,
    disposer = "disposeIt"
)
public class Implementation3 implements Interface {

    void disposeIt(String value) {}

}