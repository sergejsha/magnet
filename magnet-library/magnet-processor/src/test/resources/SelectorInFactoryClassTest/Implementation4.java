package selector;

import magnet.Instance;

@Instance(
    types = Interface.class,
    selector = "android.kaboom > 25"
)
class Implementation4 implements Interface {}