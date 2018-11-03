package magnet

/** Creates new scope and calls given init-function. */
inline fun <reified T> createScope(init: T.() -> Unit): T =
    Magnet.createScope(T::class.java).apply(init)
