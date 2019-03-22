package app;

import magnet.Instance;

@Instance(type = UnderTest.class)
public class UnderTest<T extends Foo> {

    public UnderTest(T dep) {
    }

}