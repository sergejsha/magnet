package app;

import magnet.Instance;

import java.util.List;

@Instance(type = UnderTest.class)
public class UnderTest {

    public UnderTest(List<? extends Foo> dep) {
    }

}