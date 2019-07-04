package app;

import magnet.Instance;
import magnet.Scope;

@Instance(
    type = UnderTest.class,
    limit = "activity"
)
public class UnderTest {
    public UnderTest(Scope scope) {
    }
}