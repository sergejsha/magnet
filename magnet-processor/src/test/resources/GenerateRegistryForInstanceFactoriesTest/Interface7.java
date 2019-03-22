package test;

import magnet.Instance;

interface Interface7 {

    @Instance(type = Interface7.class)
    class Implementation7 implements Interface7 {}

}