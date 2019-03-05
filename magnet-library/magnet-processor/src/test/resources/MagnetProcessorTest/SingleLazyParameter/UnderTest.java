package app;

import kotlin.Lazy;
import magnet.Instance;
import org.jetbrains.annotations.Nullable;

@Instance(type = UnderTest.class)
public class UnderTest {

    public UnderTest(Lazy<String> dep) {
    }

}