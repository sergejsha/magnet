package test;

import magnet.Factory;
import magnet.Scope;
import magnet.Scoping;
import org.jetbrains.annotations.NotNull;

class CustomFactory1 implements Factory<Interface1> {

    @NotNull
    @Override
    public Interface1 create(
        @NotNull Scope scope,
        @NotNull Class<Interface1> type,
        @NotNull String classifier,
        @NotNull Scoping scoping,
        @NotNull Instantiator<Interface1> instantiator
    ) {
        return null;
    }
}