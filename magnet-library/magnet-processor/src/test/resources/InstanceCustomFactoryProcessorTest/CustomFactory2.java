package test;

import magnet.Factory;
import magnet.ScopeContainer;

class CustomFactory2<T> implements Factory<T> {
    @Override
    public T create(ScopeContainer scope, Class<T> type, String classifier) {
        return null;
    }
}