package app.test;

import app.Interface1;
import magnet.Instance;

@Instance(
    type = Interface1.class,
    classifier = "implementation1"
)
class Implementation1 implements Interface1 {

    Implementation1() {}

    @Override
    public int getId() {
        return 0;
    }

}