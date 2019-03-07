package magnet.internal

import magnet.Scope

class OptionalLazy<T>(
    private val scope: Scope,
    private val type: Class<T>,
    private val classifier: String
) : Lazy<T?> {
    private var _value: T? = null
    private var initialized = false

    override val value: T?
        get() {
            if (!initialized) {
                _value = scope.getOptional(type, classifier)
                initialized = true
            }
            return _value
        }

    override fun isInitialized(): Boolean = initialized
}
