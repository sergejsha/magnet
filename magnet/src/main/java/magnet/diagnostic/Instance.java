package magnet.diagnostic;

import magnet.Scoping;
import org.jetbrains.annotations.NotNull;

public interface Instance {

    enum Provision { BOUND, INJECTED }

    @NotNull
    Scoping getScoping();

    @NotNull
    String getClassifier();

    @NotNull
    Class<?> getType();

    @NotNull
    Object getValue();

    @NotNull
    Provision getProvision();

}
