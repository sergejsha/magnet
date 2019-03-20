package app;

import kotlin.Lazy;
import kotlin.Metadata;
import magnet.Instance;
import org.jetbrains.annotations.NotNull;

@Metadata(
    mv = {1, 1, 13},
    bv = {1, 0, 3},
    k = 2,
    d1 = {"\u0000\u0012\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0000\u001a\u0018\u0010\u0000\u001a\u00020\u00012\u000e\u0010\u0002\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00040\u0003H\u0007¨\u0006\u0005"},
    d2 = {"provideUnderTest", "Lapp/UnderTest;", "dep", "Lkotlin/Lazy;", "", "magnet-processor"}
)
public class UnderTest {

    @Instance(type = UnderTest.class)
    public static UnderTest provideUnderTest(@NotNull Lazy<String> dep) {
        return new UnderTest();
    }

}