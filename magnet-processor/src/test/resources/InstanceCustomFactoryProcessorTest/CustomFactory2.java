package test;

import magnet.Factory;
import magnet.Scope;
import magnet.Scoping;
import org.jetbrains.annotations.NotNull;

class CustomFactory2<T> implements Factory<T> {

    @NotNull
    @Override
    public T create(
        @NotNull Scope scope,
        @NotNull Class<T> type,
        @NotNull String classifier,
        @NotNull Scoping scoping,
        @NotNull Instantiator<T> instantiator) {
        return null;
    }
}