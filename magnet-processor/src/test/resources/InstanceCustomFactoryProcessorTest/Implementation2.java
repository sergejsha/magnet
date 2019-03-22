package test;

import magnet.Instance;

@Instance(
    type = Interface2.class,
    factory = CustomFactory2.class
)
public class Implementation2 implements Interface2 {}