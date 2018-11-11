package test;

import magnet.Factory;
import magnet.Scope;
import magnet.Scoping;
import org.jetbrains.annotations.NotNull;

class CustomFactory3 implements Factory<Interface3> {

    @NotNull
    @Override
    public Interface3 create(
        @NotNull Scope scope,
        @NotNull Class<Interface3> type,
        @NotNull String classifier,
        @NotNull Scoping scoping,
        @NotNull Instantiator<Interface3> instantiator
    ) {
        return null;
    }
}