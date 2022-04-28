package magnet.kotlin.internal

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.same
import com.nhaarman.mockitokotlin2.verify
import magnet.Scope
import magnet.internal.ManyLazy
import org.junit.Test

class ManyLazyTest {

    private val value = listOf("value")
    private val type = String::class.java
    private val classifier = "classifier"
    private val scope: Scope = mock {
        on { getMany(same(type), same(classifier)) }.thenReturn(value)
    }

    private val underTest: Lazy<List<String>> = ManyLazy(scope, type, classifier)

    @Test
    fun `Constructor doesn't call scope_getMany()`() {
        verify(scope, never()).getMany(type, classifier)
    }

    @Test
    fun `value calls scope_getMany()`() {
        underTest.value
        verify(scope).getMany(type, classifier)
    }

    @Test
    fun `value calls scope_getMany() once`() {
        underTest.value
        underTest.value
        verify(scope).getMany(type, classifier)
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