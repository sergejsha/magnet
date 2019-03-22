package siblings;

import magnet.Instance;

@Instance(type = Interface1.class, types = {Interface1.class, Interface2.class})
class Implementation1 implements Interface1, Interface2 {}