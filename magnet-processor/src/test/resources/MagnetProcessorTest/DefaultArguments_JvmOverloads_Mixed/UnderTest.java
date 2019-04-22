package app;

import kotlin.Metadata;
import magnet.Instance;
import org.jetbrains.annotations.NotNull;

@Metadata(
    mv = {1, 1, 15},
    bv = {1, 0, 3},
    k = 1,
    d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B+\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0003¢\u0006\u0002\u0010\u0007R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0003X\u0082\u0004¢\u0006\u0002\n\u0000¨\u0006\b"},
    d2 = {"Lapp/UnderTest;", "", "value1", "", "value2", "value3", "value4", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "magnet-processor"}
)
@Instance(
    type = UnderTest.class
)
public final class UnderTest {

    public UnderTest(@NotNull String value1, @NotNull String value2, @NotNull String value3, @NotNull String value4) {
        super();
    }

    public UnderTest(@NotNull String value1, @NotNull String value2, @NotNull String value3) {
        super();
    }

    public UnderTest(@NotNull String value1, @NotNull String value3) {
        super();
    }
}
