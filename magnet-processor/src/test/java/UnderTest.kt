package app

import magnet.Instance
import magnet.Scope

@Instance(type = UnderTest::class)
class UnderTest constructor(
    private val parentScope: Scope,
    private val value: String = "default"
) {

    constructor(parentScope: Scope) : this(parentScope, "default")

}