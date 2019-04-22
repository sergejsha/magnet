package app;

import kotlin.Metadata;
import magnet.Instance;
import magnet.Scope;
import org.jetbrains.annotations.NotNull;

@Metadata(
    mv = {1, 1, 15},
    bv = {1, 0, 3},
    k = 1,
    d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0016\u0012\u0006\u0010\u0002\u001a\u00020\u0003¢\u0006\u0002\u0010\u0004B\u000f\b\u0016\u0012\u0006\u0010\u0005\u001a\u00020\u0006¢\u0006\u0002\u0010\u0007¨\u0006\b"},
    d2 = {"Lapp/UnderTest;", "", "value", "", "(Ljava/lang/String;)V", "parentScope", "Lmagnet/Scope;", "(Lmagnet/Scope;)V", "magnet-processor"}
)
@Instance(
    type = UnderTest.class
)
public final class UnderTest {
    public UnderTest(@NotNull String value) {
        super();
    }

    public UnderTest(@NotNull Scope parentScope) {
        super();
    }
}
