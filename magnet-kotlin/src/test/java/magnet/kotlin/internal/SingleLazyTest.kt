package magnet.kotlin.internal

import com.google.common.truth.Truth.assertThat
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.same
import org.mockito.kotlin.verify
import magnet.Scope
import magnet.internal.SingleLazy
import org.junit.Test

class SingleLazyTest {

    private val value = "value"
    private val type = String::class.java
    private val classifier = "classifier"
    private val scope: Scope = mock {
        on { getSingle(same(type), same(classifier)) }.thenReturn(value)
    }

    private val underTest: Lazy<String> = SingleLazy(scope, type, classifier)

    @Test
    fun `Constructor doesn't call scope_getSingle()`() {
        verify(scope, never()).getSingle(type, classifier)
    }

    @Test
    fun `value calls scope_getSingle()`() {
        underTest.value
        verify(scope).getSingle(type, classifier)
    }

    @Test
    fun `value calls scope_getSingle() once`() {
        underTest.value
        underTest.value
        verify(scope).getSingle(type, classifier)
    }

    @Test
    fun `value returns value`() {
        assertThat(underTest.value).isSameInstanceAs(value)
    }

    @Test
    fun `isInitialized() returns false when not initialized`() {
        assertThat(underTest.isInitialized()).isFalse()
    }

    @Test
    fun `isInitialized() returns true when initialized`() {
        underTest.value
        assertThat(underTest.isInitialized()).isTrue()
    }
}