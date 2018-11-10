package test;

import magnet.Factory;
import magnet.Scope;
import magnet.Scoping;

class CustomFactory2<T> implements Factory<T> {
    @Override
    public T create(Scope scope, Class<T> type, String classifier, Scoping scoping, Instantiator<T> instantiator) {
        return null;
    }
}