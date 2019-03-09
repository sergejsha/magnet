package magnet.internal

import magnet.Scope

class ManyLazy<T>(
    private val scope: Scope,
    private val type: Class<T>,
    private val classifier: String
) : Lazy<List<T>> {
    private var _value: List<T>? = null

    override val value: List<T>
        get() {
            if (_value == null) _value = scope.getMany(type, classifier)
            @Suppress("UNCHECKED_CAST") return _value as List<T>
        }

    override fun isInitialized(): Boolean = _value != null
}
