package magnet

inline fun <reified T> createRootScope(init: T.() -> Unit): T = Magnet.createRootScope(T::class.java).apply(init)
inline fun <reified T> createRootScope(): T = Magnet.createRootScope(T::class.java)
