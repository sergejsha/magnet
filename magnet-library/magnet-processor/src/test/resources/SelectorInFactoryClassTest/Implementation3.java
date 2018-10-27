package selector;

import magnet.Instance;

@Instance(
    types = Interface.class,
    selector = ".api > 25"
)
class Implementation3 implements Interface {}