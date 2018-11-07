package test;

import magnet.Factory;
import magnet.ScopeContainer;

class CustomFactory1 implements Factory<Interface1> {
    @Override
    public Interface1 create(ScopeContainer scope, Class<Interface1> type, String classifier) {
        return null;
    }
}