package test;

import magnet.Factory;
import magnet.Scope;

class CustomFactory3 implements Factory<Interface3> {
    @Override
    public Interface3 create(Scope scope, Class<Interface3> type, String classifier, Instantiator<Interface3> instantiator) {
        return null;
    }
}