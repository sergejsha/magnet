package test;

import magnet.Instance;

interface Interface1 {

    @Instance(type = Interface1.Delegate.class)
    class Delegate {}
}