package selector;

import magnet.Instance;

@Instance(
    types = Interface.class,
    selector = "android.api != 19"
)
class Implementation9 implements Interface {}