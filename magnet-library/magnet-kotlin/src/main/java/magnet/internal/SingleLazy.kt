package magnet.internal

import magnet.Scope

class SingleLazy<T : Any>(
    private val scope: Scope,
    private val type: Class<T>,
    private val classifier: String
) : Lazy<T> {
    private var _value: T? = null

    override val value: T
        get() {
            if (_value == null) {
                _value = scope.getSingle(type, classifier)
            }
            return _value as T
        }

    override fun isInitialized(): Boolean = _value != null
}
