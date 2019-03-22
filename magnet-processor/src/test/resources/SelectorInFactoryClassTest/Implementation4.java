package selector;

import magnet.Instance;

@Instance(
    types = Interface.class,
    selector = "android. > 25"
)
class Implementation4 implements Interface {}