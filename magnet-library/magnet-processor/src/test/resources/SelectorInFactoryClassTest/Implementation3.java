package selector;

import magnet.Instance;

@Instance(
    types = Interface.class,
    selector = "kaboom.api > 25"
)
class Implementation3 implements Interface {}