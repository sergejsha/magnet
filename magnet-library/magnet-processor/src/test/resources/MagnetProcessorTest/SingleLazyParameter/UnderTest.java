package app;

import kotlin.Lazy;
import magnet.Instance;

@Instance(type = UnderTest.class)
public class UnderTest {

    public UnderTest(Lazy<String> dep) {
    }

}