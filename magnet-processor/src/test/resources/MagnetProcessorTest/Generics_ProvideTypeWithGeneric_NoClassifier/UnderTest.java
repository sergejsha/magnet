package app;

import magnet.Instance;

class UnderTest {

    @Instance(type = Type.class)
    public static Type<Parameter> provideType() {
        return new Type<Parameter>() {
        };
    }

}