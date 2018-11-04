package magnet.processor

import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.CompilationSubject.assertThat
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import org.junit.Test
import javax.tools.JavaFileObject

class ScopeInstanceProcessorTest {

    private fun withResource(name: String): JavaFileObject =
        JavaFileObjects.forResource(javaClass.simpleName + '/' + name)

    @Test
    fun `Getters in single interface`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Scope1.java")
            )
        assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("test/MagnetInstanceScope1")
            .hasSourceEquivalentTo(withResource("expected/MagnetInstanceScope1.java"))

    }

    @Test
    fun `Binders in single interface`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Scope2.java")
            )
        assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("test/MagnetInstanceScope2")
            .hasSourceEquivalentTo(withResource("expected/MagnetInstanceScope2.java"))

    }

    @Test
    fun `Parent Scope binder`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("ParentScope.java"),
                withResource("Scope3.java")
            )
        assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("test/MagnetInstanceScope3")
            .hasSourceEquivalentTo(withResource("expected/MagnetInstanceScope3.java"))

    }

    @Test
    fun `Scope must be interface`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Scope4.java")
            )
        assertThat(compilation).hadErrorContaining("interface")

    }

    @Test
    fun `Getters in inherited interfaces`() {
        val compilation = Compiler.javac()
            .withProcessors(MagnetProcessor())
            .compile(
                withResource("Scope5_1.java"),
                withResource("Scope5_2.java"),
                withResource("Scope5_3.java"),
                withResource("Scope5.java")
            )
        assertThat(compilation).succeeded()

        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("test/MagnetInstanceScope5")
            .hasSourceEquivalentTo(withResource("expected/MagnetInstanceScope5.java"))

    }

}