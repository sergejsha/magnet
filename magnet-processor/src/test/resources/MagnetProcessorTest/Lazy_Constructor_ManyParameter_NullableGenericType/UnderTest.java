package app;

import kotlin.Lazy;
import kotlin.Metadata;
import magnet.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Metadata(
    mv = {1, 1, 13},
    bv = {1, 0, 3},
    k = 1,
    d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u001b\u0012\u0014\u0010\u0002\u001a\u0010\u0012\f\u0012\n\u0012\u0006\u0012\u0004\u0018\u00010\u00050\u00040\u0003¢\u0006\u0002\u0010\u0006¨\u0006\u0007"},
    d2 = {"Lapp/UnderTest;", "", "dep", "Lkotlin/Lazy;", "", "", "(Lkotlin/Lazy;)V", "de.halfbit.magnet-processor.main"}
)
@Instance(type = UnderTest.class)
public class UnderTest {

    public UnderTest(@NotNull Lazy<List<String>> dep) {
    }

}