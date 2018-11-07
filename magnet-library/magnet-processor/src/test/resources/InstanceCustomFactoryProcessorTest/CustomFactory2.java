package test;

import magnet.Factory;
import magnet.Scope;

class CustomFactory2<T> implements Factory<T> {
    @Override
    public T create(Scope scope, Class<T> type, String classifier) {
        return null;
    }
}