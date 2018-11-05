package magnet

inline fun <reified T> createScope(init: T.() -> Unit): T = Magnet.createScope(T::class.java).apply(init)
inline fun <reified T> createScope(): T = Magnet.createScope(T::class.java)
