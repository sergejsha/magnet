package app;

import magnet.Instance;

@Instance(type = UnderTest.class)
class UnderTest {
    public UnderTest(Dependency<Thread> dependency) { }
}