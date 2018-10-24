package selector;

import magnet.Instance;

@Instance(
    types = Interface.class,
    selector = "android.api > kaboom"
)
class Implementation6 implements Interface {}