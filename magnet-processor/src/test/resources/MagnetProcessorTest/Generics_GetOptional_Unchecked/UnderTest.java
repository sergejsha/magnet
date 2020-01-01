package app;

import org.jetbrains.annotations.Nullable;

import magnet.Instance;

@Instance(type = UnderTest.class)
class UnderTest {
    public UnderTest(@Nullable Dependency<Thread> dependency) { }
}