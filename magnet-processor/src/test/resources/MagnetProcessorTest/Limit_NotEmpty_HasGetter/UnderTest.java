package app;

import magnet.Instance;
import magnet.Scope;

@Instance(
    type = UnderTest.class,
    limitedTo = "activity"
)
public class UnderTest {
    public UnderTest(Scope scope) {
    }
}