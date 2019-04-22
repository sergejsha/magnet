package app;

import magnet.Instance;
import magnet.Scope;

@Instance(type = UnderTest.class)
class UnderTest {

    UnderTest(Scope scope) { }
    UnderTest() { }

}
