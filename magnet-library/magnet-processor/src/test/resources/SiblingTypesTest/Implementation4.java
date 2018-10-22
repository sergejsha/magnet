package siblings;

import magnet.Instance;

@Instance(types = {Interface1.class, Interface2.class})
class Implementation4 implements Interface1, Interface2 {}