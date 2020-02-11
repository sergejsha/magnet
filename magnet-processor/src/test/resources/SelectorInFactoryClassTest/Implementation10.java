package selector;

import magnet.Instance;

@Instance(
    types = Interface.class,
    selector = "android.api !in 5..10"
)
class Implementation10 implements Interface {}