package magnet

import magnet.internal.InstanceScope

/** Creates new root scope and initializes it using given init-function. */
inline fun createRootScope(init: InstanceScope.() -> Unit): InstanceScope {
    return Magnet.createRootScope().apply(init)
}

/** Creates new root scope. */
fun createRootScope(): InstanceScope {
    return Magnet.createRootScope()
}
