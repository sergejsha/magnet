package selector;

import magnet.Instance;

@Instance(
    types = Interface.class,
    selector = "android.api in 1..19"
)
class Implementation8 implements Interface {}