package selector;

import magnet.Instance;

@Instance(
    types = Interface.class,
    selector = "kaboom"
)
class Implementation2 implements Interface {}