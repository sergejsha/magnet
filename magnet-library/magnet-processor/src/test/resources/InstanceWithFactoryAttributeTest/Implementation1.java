package test;

import magnet.Instance;

@Instance(
    type = Interface1.class,
    factory = CustomFactory1.class
)
public class Implementation1 implements Interface1 {}