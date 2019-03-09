package app

import magnet.Instance

@Instance(type = UnderTest::class)
class UnderTest(dep: Lazy<List<String>>)
