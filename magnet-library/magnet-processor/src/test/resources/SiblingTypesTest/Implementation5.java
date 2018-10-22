package siblings;

import magnet.Instance;
import magnet.Scoping;

@Instance(types = {Interface1.class, Interface2.class}, scoping = Scoping.UNSCOPED)
class Implementation5 implements Interface1, Interface2 {}