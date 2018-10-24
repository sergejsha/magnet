package selector;

import magnet.Instance;

@Instance(
    types = Interface.class,
    selector = "android.api >= 28"
)
class Implementation7 implements Interface {}