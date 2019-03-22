package app;

import kotlin.Lazy;
import magnet.Instance;
import org.jetbrains.annotations.NotNull;

public class UnderTest {

    @Instance(type = UnderTest.class)
    public static UnderTest provideUnderTest(@NotNull Lazy<String> dep) {
        return UnderTest();
    }

}