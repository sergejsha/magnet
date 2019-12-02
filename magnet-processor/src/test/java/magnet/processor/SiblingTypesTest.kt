package magnet.processor

import com.google.testing.compile.CompilationSubject.assertThat
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import org.junit.Test
import javax.tools.JavaFileObject

class SiblingTypesTest {

    @Test
    fun `Either type() or types() is required`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface1.java"),
                withResource("Interface2.java"),
                withResource("Implementation1.java")
            )

        assertThat(compilation).failed()
        assertThat(compilation).hadErrorContaining("must declare")
    }

    @Test
    fun `Both type() or types() are not allowed`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface1.java"),
                withResource("Interface2.java"),
                withResource("Implementation2.java")
            )

        assertThat(compilation).failed()
        assertThat(compilation).hadErrorContaining("not both")
    }

    @Test
    fun `Inheritance verification fails for types()`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface1.java"),
                withResource("Interface2.java"),
                withResource("Implementation3.java")
            )

        assertThat(compilation).failed()
        assertThat(compilation).hadErrorContaining("must implement siblings.Interface2")
    }

    @Test
    fun `types() generates multiple factories`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface1.java"),
                withResource("Interface2.java"),
                withResource("Implementation4.java")
            )

        assertThat(compilation).succeededWithoutWarnings()

        assertThat(compilation)
            .generatedSourceFile("siblings/Implementation4Interface1MagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/Implementation4Interface1MagnetFactory.java"))

        assertThat(compilation)
            .generatedSourceFile("siblings/Implementation4Interface2MagnetFactory")
            .hasSourceEquivalentTo(withResource("generated/Implementation4Interface2MagnetFactory.java"))
    }

    @Test
    fun `types() must be used with scoped instances`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Interface1.java"),
                withResource("Interface2.java"),
                withResource("Implementation5.java")
            )

        assertThat(compilation).failed()
        assertThat(compilation).hadErrorContaining("must be used with scoped instances")
    }

    private fun withResource(name: String): JavaFileObject =
        JavaFileObjects.forResource(javaClass.simpleName + '/' + name)
}
