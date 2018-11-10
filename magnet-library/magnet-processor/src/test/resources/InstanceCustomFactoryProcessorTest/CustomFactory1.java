package test;

import magnet.Factory;
import magnet.Scope;
import magnet.Scoping;

class CustomFactory1 implements Factory<Interface1> {
    @Override
    public Interface1 create(
        Scope scope, Class<Interface1> type, String classifier, Scoping scoping, Instantiator<Interface1> instantiator
    ) {
        return null;
    }
}