package test;

import magnet.Factory;
import magnet.Scope;

class CustomFactory1 implements Factory<Interface1> {
    @Override
    public Interface1 create(Scope scope, Class<Interface1> type, String classifier) {
        return null;
    }
}