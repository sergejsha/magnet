package selector;

import magnet.Instance;

@Instance(
    types = Interface.class,
    selector = "android.api kaboom 25"
)
class Implementation5 implements Interface {}