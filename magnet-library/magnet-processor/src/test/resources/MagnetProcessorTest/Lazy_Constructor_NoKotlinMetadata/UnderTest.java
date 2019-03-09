package app;

import kotlin.Lazy;
import magnet.Instance;
import org.jetbrains.annotations.NotNull;

@Instance(type = UnderTest.class)
public class UnderTest {

    public UnderTest(@NotNull Lazy<String> dep) {
    }

}