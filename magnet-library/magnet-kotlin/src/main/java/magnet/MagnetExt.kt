package magnet

/** Creates new root scope and initializes it using given init-function. */
inline fun createRootScope(init: Scope.() -> Unit): Scope {
    return Magnet.createRootScope().apply(init)
}

/** Creates new root scope. */
fun createRootScope(): Scope {
    return Magnet.createRootScope()
}
