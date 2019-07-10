package app;

import magnet.Instance;
import magnet.Scope;

@Instance(
    type = UnderTest.class,
    limitedTo = ""
)
public class UnderTest {
    public UnderTest(Scope scope) {
    }
}