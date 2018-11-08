package test;

import magnet.Instance;

@Instance(
    type = Interface3.class,
    factory = CustomFactory3.class
)
public class Implementation3 implements Interface3 {

    public Implementation3(String value1, Long value2) {}

}