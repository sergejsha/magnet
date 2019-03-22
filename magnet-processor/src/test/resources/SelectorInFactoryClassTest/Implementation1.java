package selector;

import magnet.Instance;

@Instance(
    types = Interface.class,
    selector = ""
)
class Implementation1 implements Interface {}